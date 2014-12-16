from django.core.cache import cache
from django.http.response import HttpResponse
from django.shortcuts import render, get_object_or_404
from django.views.decorators.cache import cache_page
import itertools
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

def article(request, slug):
  comments = Comment.objects.select_related('parent_id').filter(article__id=1).as_tree()
  return render(request, 'main/article.html', locals())

def react(request):
  return render(request, 'main/react.html', locals())

# for k,v in lo
