from django.conf.urls import patterns, include, url
from temp_poc import views

urlpatterns = patterns('',
    url(r'^$', views.index, name='ignoreme'),
)
