from datetime import datetime
import json
from rest_framework import serializers
from main.models import Article


class ArticleSerializer(serializers.ModelSerializer):
  class Meta:
    model = Article


class PublishedArticleSerializer(serializers.ModelSerializer):
  class Meta:
    model = Article

  title = serializers.CharField(required=True, allow_blank=False)
  sub_title = serializers.CharField(required=True, allow_blank=False)
  slug = serializers.CharField(required=True, allow_blank=False)
  working_copy = serializers.CharField(required=True, allow_blank=False)


  def update(self, instance, validated_data):
    instance.body = validated_data.get('working_copy')
    instance.pub_date = datetime.now()
    instance.published = True
    instance.save()
    return instance




