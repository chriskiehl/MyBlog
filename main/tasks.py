from __future__ import absolute_import
import multiprocessing

from blog.celery import app

from django.conf import settings
from django.core import urlresolvers
from django.core.urlresolvers import reverse
from django.test.client import RequestFactory
from django.core.cache import cache

from main import util
from main.models import Article


# class Worker(multiprocessing.Process):
#   def __init__(self, queue):
#     multiprocessing.Process.__init__(self)
#     import django
#     django.setup()
#     from django.db import connection
#     self.queue = queue
#
#   def run(self):
#     while True:
#       try:
#         item = self.queue.get(timeout=10)
#         if item == 'shutdown':
#           break
#       except:
#         pass
#
#
#
#
# class TaskQueue(object):
#   __state = {}
#   def __init__(self, queue):
#     self.__dict__ = self.__state
#     if not getattr(self, 'queue'):
#       self.queue = multiprocessing.Queue()
#       self.worker = Worker(self.queue)
#       self.worker.start()
#
#
#   def apply_async(self, taskname):
#     self.queue.put(taskname)
#

request_factory = RequestFactory()

@app.task
def update_cache(*args, **kwargs):
  update_articles()
  update_homepage()


def update_homepage():
  url = reverse('home')
  response = process_view(url)
  write_cache(response)


def update_articles():
  article_slugs = Article.objects.filter(stale=True, published=True).values_list('slug', flat=True)
  for slug in article_slugs:
    url = reverse('view_article', kwargs={'slug': slug})
    response = process_view(url)
    write_cache(response, slug)


def process_view(url):
  callback, callback_args, callback_kwargs = resolve_request(url)
  return callback(request_factory.get(url, {'cache_update': True}), *callback_args, **callback_kwargs)

def write_cache(response, *args):
  cache_key = util.build_cache_key(*args)
  cache.set(cache_key, response, 99999)


def resolve_request(path):
  urlconf = settings.ROOT_URLCONF
  urlresolvers.set_urlconf(urlconf)
  resolver = urlresolvers.RegexURLResolver(r'^/', urlconf)
  resolver_match = resolver.resolve(path)
  return resolver_match



