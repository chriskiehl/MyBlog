from django.conf.urls import patterns, include, url
from main import views

urlpatterns = patterns('',
    url(r'^$', views.index, name='home'),
    url(r'^sitemap.xml$', 'main.views.sitemap', name='sitemap'),
    url(r'^rss.xml$', 'main.views.rss', name='rss'),
    url(r'^backlog/page/(?P<page>[\d]+)', 'main.views.backlog', name='backlog-paginator'),
    url(r'^article/(?P<slug>[\w-]+)/$', views.show_article, name='view_article'),

    url(r'^api/articles$', views.ArticleList.as_view(), name='articles'),
    url(r'^api/articles/(?P<pk>[0-9]+)$', views.ArticleDetail.as_view(), name='articles_detail'),
    url(r'^api/articles/(?P<pk>[0-9]+)/publish$', views.ArticlePublish.as_view(), name='article_publish'),

)
