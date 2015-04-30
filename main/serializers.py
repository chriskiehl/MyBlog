from django.contrib.auth.models import User
from rest_framework import serializers
from rest_framework.permissions import IsAuthenticated
from main.models import Article

#
# class UserSerializer(serializers.ModelSerializer):
#     articles = serializers.PrimaryKeyRelatedField(many=True, queryset=Article.objects.all())
#
#     class Meta:
#         model = User
#         fields = ('id', 'username', 'articles')

class ArticleSerializer(serializers.ModelSerializer):
  class Meta(object):
    model = Article
    fields = (
      'id',
      'title_image',
      'thumbnail',
      'slug',
      'title',
      'sub_title',
      'body',
      'working_draft',
      'pub_date',
      'last_modified',
      'views',
      'should_display_comments',
      'published',
      'tags',
      'related',
      'generate_related',
    )

