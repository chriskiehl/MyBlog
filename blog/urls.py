from django.conf.urls import patterns, include, url
from django.contrib import admin
from django.contrib.admin.options import ModelAdmin

urlpatterns = patterns('',
    url(r'^$', 'main.views.index', name='home'),
    url(r'^sitemap.xml$', 'main.views.sitemap', name='sitemap'),
    url(r'^article/', include('main.urls')),
    url(r'^backlog/page/(?P<page>[\d]+)', 'main.views.backlog', name='backlog-paginator'),
    url(r'^admin/', include(admin.site.urls)),
)
