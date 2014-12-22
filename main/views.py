import json
import datetime
from django.core.cache import cache
from django.http.response import HttpResponse
from django.shortcuts import render, get_object_or_404
from django.views.decorators.cache import cache_page
import itertools
from django.views.decorators.csrf import ensure_csrf_cookie
from main.models import Article, Comment


def reader():
    """A generator that fakes a read from a file, socket, etc."""
    for i in range(4):
        yield '<< %s' % i

def reader_wrapper(g):
    # Manually iterate over data produced by reader
    return [1,2]

def cache_get_or_store(key, action, timeout=5):
  cache_item = cache.get(key)
  if not cache_item:
    print 'Cache miss for', key
    cache.set(key, action(), timeout)
  cache_item = cache.get(key)
  return cache_item

def index(request):
  most_recent = Article.objects.all().order_by('-pub_date')
  most_popular = Article.objects.all().order_by('-views')[:3]
  comments = Comment.objects.select_related('parent_id').filter(article__id=1).as_tree()
  return render(request, 'main/index.html', locals())


@ensure_csrf_cookie
def article(request, slug):
  article = get_object_or_404(Article, slug=slug)
  comments = Comment.objects.select_related('parent_id').filter(article__id=1).as_tree()
  return render(request, 'main/article.html', locals())


def store_comment(request, slug):
  JsonResponse = lambda x: HttpResponse(json.dumps(x), content_type="application/json")
  if request.method != 'POST':
    return JsonResponse({'response': 'invalid request'})

  required = ('parent_id', 'body', 'name')
  params = [request.POST.get(item, None) for item in required]
  if not all(params):
    missing = ', '.join(p for p in required if request.POST.get(p, None) is None)
    return JsonResponse({'response': 'Missing required param(s): {0}'.format(missing)})

  article = Article.objects.get(slug=slug)
  parent = Comment.objects.get(id=int(request.POST['parent_id']))
  comment = Comment.create(
      article,
      parent,
      request.POST['name'],
      request.POST['body']
  )
  print 'saving comment'
  # comment.save()
  return JsonResponse(dict(request.POST.items() + [('parent_id', 10)]))





def react(request):
  return render(request, 'main/react.html', locals())

# for k,v in lo
