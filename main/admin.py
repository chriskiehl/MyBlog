import json
from django.conf import settings
from django.conf.urls import patterns
from django.contrib import admin
from django import forms
from django.http import HttpResponse
from django.http.response import Http404

from main.models import Article, Tag


class JsonResponse(HttpResponse):
    def __init__(self, data, **kwargs):
        content = json.dumps(data)
        kwargs['content_type'] = 'application/json'
        super(JsonResponse, self).__init__(content, **kwargs)


def patch_article(request, article_id):
  body = request.POST.get('body', None)
  try:
    article = Article.objects.get(id=article_id)
    article.working_copy = body
    article.save()
    return JsonResponse({'success': 'updated'}, status=204)
  except Exception as e:
    return JsonResponse({'error': e.message}, status=400)


def new_article(request):
  if not request.POST:
    JsonResponse({'error': 'Invalid Request'}, status=400)
  else:
    article = Article.objects.create(title="Untitled")
    print article.id
    return JsonResponse({'id': article.id}, status=201)




def get_admin_urls(urls):
    def get_urls():
        my_urls = patterns('',
           (r'^main/article/(?P<article_id>\d+)/patch-article/$', admin.site.admin_view(patch_article)),
           (r'^main/article/new-article/$', admin.site.admin_view(new_article)),
        )
        return my_urls + urls
    return get_urls


class TagAdmin(forms.ModelForm):
  model = Article.tags.through


class RelatedAdminForm(forms.ModelForm):
  class Meta:
    model = Article
    fields = (
      'working_copy',
      'title_image',
      'title',
      'sub_title',
      'slug',
      'pub_date',
      'last_modified',
      'views',
      'should_display_comments',
      'published',
      'generate_related',
      'related',
      'tags',
    )

  # def __init__(self, *args, **kwargs):
  #   super(RelatedAdminForm, self).__init__(*args, **kwargs)
  #   if self.instance.id:
  #     self.fields['related'].queryset = Article.objects.filter(tags__in=self.instance.tags.all()).distinct()

  def clean(self):
    super(RelatedAdminForm, self).clean()


class ArticleForm(forms.ModelForm):
  class Meta:
    model = Article
    fields = '__all__'


class ArticleAdmin(admin.ModelAdmin):
  form = RelatedAdminForm

  def get_readonly_fields(self, request, obj=None):
    return self.readonly_fields + ('views', 'last_modified', 'pub_date')

  def changeform_view(self, request, object_id=None, form_url='', extra_context=None):
    print request.path
    extra_context = extra_context or {}
    extra_context.update({
      'AWS_ACCESS_KEY_ID': settings.AWS_ACCESS_KEY_ID,
      'AWS_SECRET_ACCESS_KEY': settings.AWS_SECRET_ACCESS_KEY,
      'object_id': object_id
    })
    return super(ArticleAdmin, self).changeform_view(request, object_id, form_url, extra_context=extra_context)

  def save_related(self, request, form, formsets, change):
    article = form.instance
    generate_related = form.cleaned_data.get('generate_related')
    has_related = form.cleaned_data.get('related')

    if generate_related and (self.tags_changed(article, form) or not has_related):
      article.related = article._build_related_list()
      article.save()

    form.save_m2m()
    for formset in formsets:
        self.save_formset(request, form, formset, change=change)

  def tags_changed(self, instance, form):
    old_tags = list(instance.tags.all())
    new_tags = list(form.cleaned_data.get('tags'))
    return old_tags != new_tags





admin.site.register(Article, ArticleAdmin)
admin.site.register(Tag)
admin_urls = get_admin_urls(admin.site.get_urls())
admin.site.get_urls = admin_urls
