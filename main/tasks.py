from __future__ import absolute_import
from django.core.handlers.base import BaseHandler
from django.core.mail import send_mail
from django.http import HttpRequest

from blog.celery_stuff import app

from django.conf import settings
from django.core import urlresolvers
from django.core.urlresolvers import reverse
from django.test.client import RequestFactory, FakePayload
from django.core.cache import cache

from main import util
from main.models import Article

meta_params = [
  'HTTP_COOKIE',
  'PATH_INFO',
  'REMOTE_ADDR',
  'REQUEST_METHOD',
  'SCRIPT_NAME',
  'SERVER_NAME',
  'SERVER_PORT',
  'SERVER_PROTOCOL',
  'wsgi.version',
  'wsgi.url_scheme',
  'wsgi.multiprocess',
  'wsgi.multithread',
  'wsgi.run_once',
]




@app.task
def send_email(subject, body):
  send_mail(
    subject,
    body,
    settings.EMAIL_HOST_USER,
    [settings.EMAIL_HOST_USER]
  )



@app.task
def update_cache(request_meta, *args, **kwargs):
  request = request_factory(rebuild_request(request_meta))
  update_articles(request)
  update_homepage(request)


def request_factory(request_meta):
  environ = {param: request_meta.META[param] for param in meta_params}
  return RequestFactory(**environ)

def update_homepage(request):
  url = reverse('home')
  response = process_view(url, request)
  write_cache(response)


def update_articles(request):
  article_slugs = Article.objects.filter(stale=True, published=True).values_list('slug', flat=True)
  for slug in article_slugs:
    url = reverse('view_article', kwargs={'slug': slug})
    response = process_view(url, request)
    write_cache(response, slug)


def process_view(url, request):
  callback, callback_args, callback_kwargs = resolve_request(url)
  return callback(request.get(url, {'cache_update': True}), *callback_args, **callback_kwargs)

def write_cache(response, *args):
  cache_key = util.build_cache_key(*args)
  cache.set(cache_key, response, 99999)


def resolve_request(path):
  urlconf = settings.ROOT_URLCONF
  urlresolvers.set_urlconf(urlconf)
  resolver = urlresolvers.RegexURLResolver(r'^/', urlconf)
  resolver_match = resolver.resolve(path)
  return resolver_match


def rebuild_request(request_payload):
  request = HttpRequest()
  request.__dict__ = request_payload
  return request
