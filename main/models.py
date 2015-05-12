import os
import boto
import time
from datetime import datetime
from operator import itemgetter

from django.conf import settings
from django.db import models
from boto.s3.key import Key

from main import util


class Tag(models.Model):
  name = models.CharField(max_length=30, unique=True)

  def __unicode__(self):
    return self.name


class Article(models.Model):
  title_image = models.CharField(max_length=300, null=True, blank=True)
  thumbnail = models.CharField(max_length=300, null=True)
  title = models.CharField(max_length=300)
  sub_title = models.CharField(max_length=300, null=True, blank=True)
  slug = models.CharField(max_length=100, blank=True)
  body = models.TextField(null=True)
  working_copy = models.TextField(null=True, blank=True)
  pub_date = models.DateTimeField('date published', null=True, blank=True)
  last_modified = models.DateTimeField('last modified', default=datetime.now())
  views = models.IntegerField(default=0)
  should_display_comments = models.BooleanField(default=True)
  published = models.BooleanField(default=False)
  tags = models.ManyToManyField(Tag, blank=True)
  related = models.ManyToManyField('self', blank=True)
  generate_related = models.BooleanField(default=True)

  last_modified_header = time.time()

  def __init__(self, *args, **kwargs):
    super(Article, self).__init__(*args, **kwargs)

  def __unicode__(self):
    return "Article titled: {0}".format(self.title)

  def save(self, *args, **kwargs):
    if self.title_image and not self.thumbnail:
        self.thumbnail = self.create_thumbnail()

    if self.pk:
      instance = Article.objects.get(pk=self.pk)

      if self.title_image_changed(instance):
        self.thumbnail = self.create_thumbnail()

    self.working_copy = self.strip_padding(self.working_copy)
    self.last_modified = datetime.now()

    super(Article, self).save()

  def _build_related_list(self):
    tags = set(self.tags.all())
    semi_related = Article.objects.filter(tags__in=self.tags.all()).exclude(id=self.id).distinct()
    with_match_level = [(self.count_similar_tags(article, tags), article) for article in semi_related]
    closest_related = sorted(with_match_level, key=itemgetter(0), reverse=True)
    return [article for (rating, article) in closest_related[:3]]


  def count_similar_tags(self, article, tags):
    return len(set(article.tags.all()).intersection(tags))


  def create_thumbnail(self):
    thumbnail = util.create_thumbnail(self.title_image)
    thumbnail_name = self.generate_thumbnail_filename(self.title_image)

    s3 = boto.connect_s3(settings.AWS_ACCESS_KEY_ID, settings.AWS_SECRET_ACCESS_KEY)
    k = Key(s3.get_bucket(settings.AWS_STORAGE_BUCKET_NAME, validate=False))

    k.content_type='image/jpeg'
    k.key = thumbnail_name

    headers = {
      'Cache-Control': 'max-age=31556926',
    }
    k.set_contents_from_file(thumbnail, policy='public-read', reduced_redundancy=True)

    thumbnail_url = k.generate_url(expires_in=0, query_auth=False)
    return thumbnail_url

  def strip_padding(self, body):
    if body:
      padded_line = '<p>&nbsp;</p>'
      return '\n'.join(line for line in body.split('\n') if padded_line not in line)
    return ''

  def generate_thumbnail_filename(self, source_url):
    filename, ext = os.path.splitext(os.path.split(source_url)[-1])
    return 'main/images/{0}-thumb{1}'.format(filename, ext)

  def title_image_changed(self, instance):
    return self.title_image and instance.title_image != self.title_image

  def publishing(self, instance):
    not_previously_published = instance.published == False
    # if we're checking the published box to ON
    return not_previously_published and self.published








