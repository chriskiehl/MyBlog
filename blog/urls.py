from django.conf.urls import patterns, include, url
from django.contrib import admin

urlpatterns = patterns('',
    url(r'^$', include('main.urls')),
    url(r'^article/(?P<slug>\D+)', 'main.views.article', name='art'),
    url(r'^admin/', include(admin.site.urls)),
)
