from django.core.cache import cache
from django.http.response import HttpResponse
from django.shortcuts import render
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
  # for i in comments: print i
  # comments = iter([(1,1),(2,2),(3,3)])
  return render(request, 'main/index.html', locals())


# for k,v in lookup_table.iteritems():
#   print k, v
#
#
#
# for node in rows:
#   if node.pid == 0:
#     root = node


# def pprint(node, depth=0, ancestors=None):
#   if not ancestors:
#     ancestors = []
#   if not node.children:
#     return [ancestors + [node.name]]
#   else:
#     output = [node.name]
#     for n in node.children:
#       output += pprint(n, depth+2, ancestors + [node.name])
#     return [] + output


# def pprint(node, depth=0):
#   if not node.children:
#     yield ['<div class="comment-box"><h4>{0}</h4><br>{1}</div>'.format(node.name, node.comment.body)]
#   else:
#     output = ['<div class="comment-box"><h4>{0}</h4><br>{1}'.format(node.name, node.comment.body)]
#     for n in node.children:
#       output = itertools.chain(output, pprint(n, depth + 1))
#     output = itertools.chain(output, ['</div>'])
#     for result in output:
#       yield result
#
#
# def get_tree_depth(node, depths=0):
#   if not node.children:
#     return depths
#   else:
#     maxdepth = 0
#     for n in node.children:
#       maxdepth = max(maxdepth, get_tree_depth(n, depth + 1))
#     return maxdepth
