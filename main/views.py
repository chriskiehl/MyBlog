from textwrap import dedent
from django.template import Context
from django.template.base import Template
from django.core.paginator import Paginator, PageNotAnInteger, EmptyPage
from django.http.response import HttpResponse, Http404
from django.shortcuts import render, get_object_or_404
from django.views.decorators.cache import cache_page
from django.views.decorators.csrf import csrf_exempt
from rest_framework import status
from rest_framework.authentication import SessionAuthentication
from rest_framework.generics import ListCreateAPIView, RetrieveUpdateDestroyAPIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView
from main.serializers import ArticleSerializer, PublishedArticleSerializer

from main.models import Article


class ArticleList(ListCreateAPIView):
  queryset = Article.objects.all()
  serializer_class = ArticleSerializer

  authentication_classes = (SessionAuthentication,)
  permission_classes = (IsAuthenticated,)

class ArticleDetail(RetrieveUpdateDestroyAPIView):
  queryset = Article.objects.all()
  serializer_class = ArticleSerializer

  authentication_classes = (SessionAuthentication,)
  permission_classes = (IsAuthenticated,)


class ArticlePublish(APIView):
  def get_or_404(self, cls, pk):
    try:
      return cls.objects.get(pk=pk)
    except cls.DoesNotExist:
      raise Http404

  def put(self, request, pk, format=None):
    article = self.get_or_404(Article, pk)
    serializer = PublishedArticleSerializer(article, data=ArticleSerializer(article).data)
    if serializer.is_valid():
      serializer.save()
      return Response(status=status.HTTP_204_NO_CONTENT)
    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


  def delete(self, request, pk, format=None):
    article = self.get_or_404(Article, pk)
    article.published = False
    article.save()
    return Response(status=status.HTTP_204_NO_CONTENT)


@cache_page(10)
def index(request):
  articles = Article.objects.filter(published=True)
  most_recent = articles.order_by('-pub_date')[:3]
  most_popular = articles.order_by('-views')[:3]
  return render(request, 'main/index.html', locals())

@cache_page(10)
def show_article(request, slug):
  article = get_object_or_404(Article, slug=slug, published=True)
  related_posts = article.related.all()
  similarly_tagged = Article.objects.filter(tags__in=article.tags.all()).distinct()
  return render(request, 'main/article.html', locals())

@cache_page(10)
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

@cache_page(10)
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

@cache_page(10)
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




