from django.conf.urls import patterns, include, url
from django.contrib import admin

urlpatterns = patterns('',
    url(r'^', include('main.urls')),
    url(r'^proof-of-concept/', include('temp_poc.urls')),
    url(r'^admin/', include(admin.site.urls)),
)
