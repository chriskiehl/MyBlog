from django.conf.urls import patterns, include, url
from django.contrib import admin
from django.contrib.admin.options import ModelAdmin

from main import views

urlpatterns = patterns('',
    url(r'^$', 'main.views.index', name='home'),
    url(r'^testmang$', 'main.views.teeeeest', name='teeeeest'),
    url(r'^sitemap.xml$', 'main.views.sitemap', name='sitemap'),
    url(r'^rss.xml$', 'main.views.rss', name='rss'),
    url(r'articles/', views.ArticleList.as_view()),
    url(r'^article/', include('main.urls')),
    url(r'^backlog/page/(?P<page>[\d]+)', 'main.views.backlog', name='backlog-paginator'),
    url(r'^admin/', include(admin.site.urls)),

    url(r'^api-auth/', include('rest_framework.urls', namespace='rest_framework')),

    url(r'^users/$', views.UserList.as_view()),
    url(r'^users/(?P<pk>[0-9]+)/$', views.UserDetail.as_view()),
)

