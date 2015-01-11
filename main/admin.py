import json
from django.conf.urls import patterns
from django.contrib import admin
from django.http import HttpResponse
from django.http.response import Http404

from main.models import Article, Comment

JsonResponse = lambda d: HttpResponse(json.dumps(d), content_type="application/json")

def save_article(request, article_id):
  body = request.POST.get('body', None)
  if body:
    try:
      article = Article.objects.get(id=article_id)
      article.body = body
      article.save()
      return JsonResponse({'success': 'Saved!'})
    except Article.DoesNotExist:
      pass
  return JsonResponse({'error': 'Invalid Request'})

def get_admin_urls(urls):
    def get_urls():
        my_urls = patterns('', (r'^main/article/(?P<article_id>\d+)/save-article/$', admin.site.admin_view(save_article)))
        return my_urls + urls
    return get_urls


class CommentAdmin(admin.TabularInline):
  model = Comment

class ArticleAdmin(admin.ModelAdmin):
  fields = [
    'body',
    'title_image',
    'title',
    'sub_title',
    'slug',
    'pub_date',
    'last_modified',
    'views',
    'should_display_comments',
    'published'
  ]


  def get_readonly_fields(self, request, obj=None):
    return self.readonly_fields + ('views', 'last_modified',) # 'pub_date')


admin.site.register(Article, ArticleAdmin)
admin_urls = get_admin_urls(admin.site.get_urls())
admin.site.get_urls = admin_urls
