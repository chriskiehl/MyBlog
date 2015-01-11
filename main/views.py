import json
from django.core.cache import cache
from django.http.response import HttpResponse
from django.shortcuts import render, get_object_or_404
from django.views.decorators.cache import cache_page
import itertools
from django.views.decorators.csrf import ensure_csrf_cookie
from main.models import Article, Comment

JsonResponse = lambda x, **kwargs: HttpResponse(json.dumps(x), content_type="application/json", **kwargs)
JsonErrorResponse = lambda x: HttpResponse(json.dumps({'error': x}), content_type="application/json", status=400)

REQUIRED_PARAMS = ('parent_id', 'body', 'name')


def index(request):
  articles = Article.objects.filter(published=True)
  most_recent = articles.order_by('-pub_date')[:3]
  most_popular = articles.order_by('-views')[:3]
  return render(request, 'main/index.html', locals())


@ensure_csrf_cookie
def article(request, slug):
  article = get_object_or_404(Article, slug=slug, published=True)
  comments = Comment.objects.select_related('parent_id').filter(article=article)
  comment_tree = comments.as_tree() if article.should_display_comments else iter([])
  return render(request, 'main/article.html', locals())


def get_object_or_none(cls, **kwargs):
  try:
    return cls.objects.get(**kwargs)
  except:
    return None


def store_comment(request, slug):
  if not request.is_ajax() or not request.method == 'POST':
    return JsonErrorResponse('Invalid Request')

  if not has_required_params(request):
    return JsonErrorResponse('Missing required param(s): {0}'.format(get_missing(request)))
  try:
    article = Article.objects.get(slug=slug)
  except Article.DoesNotExist:
    return JsonErrorResponse('No matching article found')
  else:
    parent_id = int(request.POST['parent_id']) if request.POST['parent_id'] else None
    parent_comment = get_object_or_none(Comment, id=parent_id)
    comment = Comment.create(article, parent_comment, request.POST['name'], request.POST['body'])
    print 'saving comment'
    comment.save()
    comment_info = [
      ('comment_id', comment.id),
      ('parent_id',  comment.parent.id if comment.parent else '')
    ]
    response_content = dict(request.POST.items() + comment_info)
    return JsonResponse(response_content)


def has_required_params(request):
  return all(item in request.POST.keys() for item in REQUIRED_PARAMS)

def get_missing(request):
  return ', '.join(p for p in REQUIRED_PARAMS if request.POST.get(p, None) is None)
