from django.conf.urls import patterns, include, url
from main import views

urlpatterns = patterns('',
    url(r'^$', views.article, name='asdf'),
    url(r'^(?P<slug>[\w-]+)/$', views.article, name='asdff'),
    url(r'^(?P<slug>[\w-]+)/storecomment/$', views.store_comment, name='qewr'),
)
