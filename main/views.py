import functools
import urlparse
from textwrap import dedent

from django.template import Context
from django.template.base import Template
from django.views.decorators.cache import cache_page
from django.core.paginator import Paginator, PageNotAnInteger, EmptyPage
from django.http.response import HttpResponse
from django.shortcuts import render, get_object_or_404
from django.views.decorators.csrf import ensure_csrf_cookie

from tasks import *
from main.models import Article
from main.util import pickle_request


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


def index(request):
  articles = Article.objects.filter(published=True)
  most_recent = articles.order_by('-pub_date')[:3]
  most_popular = articles.order_by('-views')[:3]
  return render(request, 'main/index.html', locals())


# @cache_view
@ensure_csrf_cookie
def show_article(request, slug):
  article = get_object_or_404(Article, slug=slug, published=True)
  related_posts = article.related.all()
  similarly_tagged = Article.objects.filter(tags__in=article.tags.all()).distinct()
  return render(request, 'main/article.html', locals())


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


@cache_page(60 * 60 * 24)
def rss(request):
  template = Template(dedent('''<?xml version="1.0"?>
      <rss version="2.0">
        <channel>
          <title>ChrisKiehl.com</title>
          <link>http://chriskiehl.com/</link>
          <description>The blog where I pretend to be good at stuff</description>
          {% for show_article in articles %}
            <item>
               <title>{{ show_article.title }}</title>
               <link>{% url 'view_article' slug=show_article.slug %}</link>
               <description>{{ show_article.body|striptags|truncatechars_html:370 }}</description>
            </item>
          {% endfor %}
        </channel>
      </rss>
        '''))

  articles = Article.objects.filter(published=True).order_by('-pub_date')
  return HttpResponse(
    template.render(Context({'articles': articles})),
    content_type="application/xml"
  )

@cache_page(60 * 60 * 24)
def sitemap(request):
  xml_template = dedent('''<?xml version="1.0" encoding="UTF-8"?>
      <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
        {body}
      </urlset>
    ''')
  wrap_url = lambda loc: '<url><loc>{loc}</loc></url>'.format(loc=loc)

  static_urls = [
    'http://chriskiehl.com',
    'http://chriskiehl.com/backlog/page/1'
  ]

  dynamic_urls = [
    'http://chriskiehl.com' + reverse('view_article', kwargs={'slug':a.slug})
    for a in Article.objects.filter(published=True)
  ]

  body = '\n'.join(wrap_url(url) for url in static_urls + dynamic_urls)
  xml = xml_template.format(body=body)
  return HttpResponse(xml, content_type="application/xml")




