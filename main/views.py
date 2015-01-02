import json
import datetime
from boto.datapipeline import exceptions
from django.core.cache import cache
from django.http.response import HttpResponse
from django.shortcuts import render, get_object_or_404
from django.views.decorators.cache import cache_page
import itertools
from django.views.decorators.csrf import ensure_csrf_cookie
from main.models import Article, Comment

JsonResponse = lambda x: HttpResponse(json.dumps(x), content_type="application/json")

def cache_get_or_store(key, action, timeout=5):
  cache_item = cache.get(key)
  if not cache_item:
    print 'Cache miss for', key
    cache.set(key, action(), timeout)
  cache_item = cache.get(key)
  return cache_item

def get_object_or_none(cls, **kwargs):
  try:
    return cls.objects.get(**kwargs)
  except:
    return None


def index(request):
  most_recent = Article.objects.all().order_by('-pub_date')
  most_popular = Article.objects.all().order_by('-views')[:3]
  # comments = Comment.objects.select_related('parent_id').filter(article__id=1).as_tree()
  comments = iter([])
  return render(request, 'main/index.html', locals())


@ensure_csrf_cookie
def article(request, slug):
  article = get_object_or_404(Article, slug=slug)
  comments = Comment.objects.select_related('parent_id').filter(article=article)
  comment_tree = comments.as_tree() if article.should_display_comments else iter([])
  return render(request, 'main/article.html', locals())


def store_comment(request, slug):
  try:
    if request.method != 'POST':
      return JsonResponse({'response': 'invalid request'})

    required = ('parent_id', 'body', 'name')
    params = [item in request.POST.keys() for item in required]
    if not all(params):
      missing = ', '.join(p for p in required if request.POST.get(p, None) is None)
      return JsonResponse({'response': 'Missing required param(s): {0}'.format(missing)})
    from django.core.cache import get_cache
    c = get_cache()
    article = Article.objects.get(slug=slug)
    parent_id = int(request.POST['parent_id']) if request.POST['parent_id'] else None
    parent_comment = get_object_or_none(Comment, id=parent_id)
    comment = Comment.create(
        article,
        parent_comment,
        request.POST['name'],
        request.POST['body']
    )
    print 'saving comment'
    comment.save()
    comment_id = comment.id
    parent_id = comment.parent_id.id if comment.parent_id else ''

    print comment_id
    # comment.delete()
    return JsonResponse(dict(request.POST.items() + [('parent_id', parent_id), ('comment_id', comment_id)]))
  except Exception as e:
    print e

def editor(request):
  from django.core.cache.utils import make_template_fragment_key
  print 'view generated fragment:', make_template_fragment_key('cooltext', [])
  return render(request, 'main/editor.html', locals())

def react(request):
  return HttpResponse("Hello world!")
  # return render(request, 'main/react.html', locals())

# for k,v in lo
