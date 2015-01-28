import functools
import time
import json
import urlparse
from django.core.handlers.wsgi import WSGIRequest
from django.core.urlresolvers import reverse
from django.http.request import QueryDict
from django.test.client import RequestFactory
from django.views.decorators.cache import cache_page
from tasks import *

from django.core.paginator import Paginator, PageNotAnInteger, EmptyPage
from django.http.response import HttpResponse
from django.shortcuts import render, get_object_or_404
from django.views.decorators.csrf import ensure_csrf_cookie

from main.models import Article, Comment
from main.util import get_object_or_none, pickle_request


JsonResponse = lambda x, **kwargs: HttpResponse(json.dumps(x), content_type="application/json", **kwargs)
JsonErrorResponse = lambda x: HttpResponse(json.dumps({'error': x}), content_type="application/json", status=400)



def cache_view(view_func):
  @functools.wraps(view_func)
  def _decorate(request, *args, **kwargs):
    query_string = urlparse.parse_qs(request.GET.urlencode())
    if query_string.get('cache_update', None):
      print 'Received cache update request. Rendering response'
      return view_func(request, *args, **kwargs)

    cache_key = util.build_cache_key(*args, **kwargs)
    cached_response = cache.get(cache_key)
    if cached_response:
      print 'cache hit on Key:', cache_key
      if Article.content_out_of_date:
        print 'Cache content is stale. Submitting update task'
        print 'Article status after setting to False: {0}'.format(Article.content_out_of_date)
        update_cache.delay(pickle_request(request))
        Article.content_out_of_date = False
      return cached_response
    else:
      print 'cache miss for key:', cache_key
      response = view_func(request, *args, **kwargs)
      cache.set(cache_key, response, 99999)
      return response
  return _decorate






@cache_view
def index(request):
  articles = Article.objects.filter(published=True)
  most_recent = articles.order_by('-pub_date')[:3]
  most_popular = articles.order_by('-views')[:3]
  return render(request, 'main/index.html', locals())


@cache_view
@ensure_csrf_cookie
def article(request, slug):
  article = get_object_or_404(Article, slug=slug, published=True)
  related_posts = article.related.all()
  similarly_tagged = Article.objects.filter(tags__in=article.tags.all()).distinct()
  comments = Comment.objects.select_related('parent_id').filter(article=article)
  comment_tree = comments.as_tree() if article.should_display_comments else iter([])
  response = render(request, 'main/article.html', locals())
  return response


@cache_page(60 * 60 * 24)
def backlog(request, page):
  article_set = Article.objects.filter(published=True).order_by('-pub_date')
  if int(page) == 1:
    # Shave off the first 3 results. They were already shown on the home page
    article_set = article_set[3:]

  paginator = Paginator(article_set, 2)
  next_page = int(page) + 1
  prev_page = int(page) - 1

  try:
    articles = paginator.page(page)
  except PageNotAnInteger:
    articles = paginator.page(1)
  except EmptyPage:
    articles = paginator.page(paginator.num_pages)

  return render(request, 'main/backlog.html', locals())





REQUIRED_PARAMS = ('parent_id', 'body', 'name')

def store_comment(request, slug):
  if not request.is_ajax() or not request.method == 'POST':
    return JsonErrorResponse('Invalid Request')

  if not _has_required_params(request):
    return JsonErrorResponse('Missing required param(s): {0}'.format(_get_missing(request)))
  try:
    _article = Article.objects.get(slug=slug)
  except Article.DoesNotExist:
    return JsonErrorResponse('No matching article found')
  else:
    parent_id = int(request.POST['parent_id']) if request.POST['parent_id'] else None
    parent_comment = get_object_or_none(Comment, id=parent_id)
    comment = Comment.create(_article, parent_comment, request.POST['name'], request.POST['body'])
    print 'saving comment'
    comment.save()
    comment_info = [
      ('comment_id', comment.id),
      ('parent_id',  comment.parent.id if comment.parent else '')
    ]

    response_content = dict(request.POST.items() + comment_info)

    article_slugs = Article.objects.filter(stale=True, published=True).values_list('slug', flat=True)
    print 'Stale slugs:', article_slugs
    try:
      update_cache.delay(pickle_request(request))
    except Exception as e:
      print e

    return JsonResponse(response_content)

def _has_required_params(request):
  return all(item in request.POST.keys() for item in REQUIRED_PARAMS)

def _get_missing(request):
  return ', '.join(p for p in REQUIRED_PARAMS if request.POST.get(p, None) is None)

