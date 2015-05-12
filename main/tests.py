import json
from operator import itemgetter
from PIL import Image
from django.contrib.auth.models import User
import factory
import pytest
from rest_framework.test import APIRequestFactory, APIClient
from main import util
from random import randint
from django.core.urlresolvers import reverse
from django.test import TestCase
from main.models import Article,Tag
from django.test import Client
from itertools import product
from rest_framework import status



@pytest.mark.django_db
def test_sanity_of_core_page(client):
  def responds_ok(page):
    response = client.get(page)
    return response.status_code == 200

  assert responds_ok('/')
  assert responds_ok('/backlog/page/1')
  assert responds_ok('/sitemap.xml')
  assert responds_ok('/rss.xml')


### API ###

@pytest.fixture
def api_client():
  test_user = User.objects.create(username='test', password='test')
  client = APIClient()
  client.force_authenticate(user=test_user)
  return client


@pytest.mark.django_db
def test_sanity_api(api_client):
  # Successful but empty
  response = api_client.get('/api/articles')
  assert response.status_code == status.HTTP_200_OK
  assert len(response.data) == 0

  # 404s
  response = api_client.get('/api/articles/1')
  assert response.status_code == status.HTTP_404_NOT_FOUND

  # create
  response = api_client.post('/api/articles', {'title': 'Title'})
  assert response.status_code == status.HTTP_201_CREATED

  # / now returning results
  response = api_client.get('/api/articles')
  assert response.status_code == status.HTTP_200_OK
  assert len(response.data) == 1

  # no longer 404s
  response = api_client.get('/api/articles/1')
  assert response.status_code == status.HTTP_200_OK



@pytest.mark.django_db
def test_publish_api_rejects_incomplete_articles(api_client):
  # make the partial article
  api_client.post('/api/articles', {'title': 'Title'})
  assert api_client.get('/api/articles/1').status_code == status.HTTP_200_OK

  response = api_client.put('/api/articles/1/publish')
  missing_fields = response.data.keys()
  assert response.status_code == status.HTTP_400_BAD_REQUEST
  assert set(missing_fields) == {'working_copy', 'slug'}


@pytest.mark.django_db
def test_publish_api_processes_valid_articles(api_client):
  # make the partial article
  response = api_client.post('/api/articles', {
    'title': 'Title',
    'slug':'slug',
    'working_copy':'body text',
    'sub_title':'subtitle'
  })
  print response.data
  assert api_client.get('/api/articles/1').status_code == status.HTTP_200_OK

  # not published
  assert not Article.objects.get(pk=1).published
  response = api_client.put('/api/articles/1/publish')
  assert response.status_code == status.HTTP_204_NO_CONTENT
  # published!
  assert Article.objects.get(pk=1).published



### Thumbnail generation ###

def test_calc_aspect_ratio():
  sixteen_by_nine = Image.new('1', (16, 9))
  four_by_three = Image.new('1', (4, 3))

  assert util.calc_aspect_ratio(sixteen_by_nine) == 1.7777777777777777
  assert util.calc_aspect_ratio(four_by_three) == 1.3333333333333333

def test_crop_center():
  black, white = 0, 255
  im = Image.new('1', (50, 50), black)
  # fill a 20x20 block in the middle with white pixels
  for row, col in product(range(15, 35), range(15, 35)):
    im.putpixel((row, col), white)

  colors = sorted(im.getcolors(), key=itemgetter(1))
  assert im.size == (50, 50)
  assert len(colors) == 2
  assert colors[0] == (2100, 0)  # num of black pixels expected - white

  target_size = (20, 20)
  cropped_im = util.crop_center(im, target_size=target_size)
  colors = sorted(cropped_im.getcolors(), key=itemgetter(1))
  assert cropped_im.size == target_size
  assert len(colors) == 1  # no black pixels



# class Aasdf(TestCase):
#
#   def test_something(self):
#     c = Client()
#     response = c.get('/')
#     self.assertEqual(200, response.status_code)


# class TestAdminAjaxSave(TestCase):
#
#   def setUp(self):
#     Article.objects.create(title='cool show_article', slug='cool-show_article')
#     self.url = reverse('comment_store', kwargs={'slug': 'cool-show_article'})
#     self.data = {
#       'body': 'hello!',
#       'name': 'myname',
#       'parent_id': '123423543456'
#     }
#
#
#   def ajax_post(self, url, data=None):
#     return self.client.post(url, data=data, HTTP_X_REQUESTED_WITH='XMLHttpRequest')
#
#
#   def test_storecomment_rejects_nonajax_requests(self):
#     response = self.client.get(self.url)
#     self.assertEqual(400, response.status_code)
#
#
#   def test_comment_ajax_fails_for_non_post_request(self):
#     responses = (
#       self.client.get(self.url, HTTP_X_REQUESTED_WITH='XMLHttpRequest'),
#       self.client.put(self.url, HTTP_X_REQUESTED_WITH='XMLHttpRequest'),
#       self.client.delete(self.url, HTTP_X_REQUESTED_WITH='XMLHttpRequest'),
#     )
#
#     for response in responses:
#       self.assertEqual(400, response.status_code)
#
#
#   def test_storecomment_returns_error_on_missing_params(self):
#     response = self.ajax_post(self.url)
#     self.assertTrue(response.status_code == 400)
#     msg = json.loads(response.content)
#
#     missing_params = ('body', 'parent_id', 'name')
#     for param in missing_params:
#       self.assertTrue(param in msg.get('error'))
#
#     response2 = json.loads(self.ajax_post(self.url, data={'body': 'hello'}).content)
#     # body should now *not* be part of the missing params response
#     self.assertTrue('body' not in response2.get('error'))
#
#
#   def test_storecomment_returns_error_on_no_matching_article(self):
#     bad_url = reverse('comment_store', kwargs={'slug': 'does-not-exist'})
#     response = json.loads(self.ajax_post(bad_url, data=self.data).content)
#     self.assertEqual('No matching show_article found', response.get('error'))
#
#
#   def test_storecomment_saves_on_valid_params(self):
#     self.assertFalse(Comment.objects.filter(body='hello!').exists())
#     self.ajax_post(self.url, data=self.data)
#     self.assertTrue(Comment.objects.filter(body='hello!').exists())
#
#
# class TestArticle(TestCase):
#
#   def setUp(self):
#     pass
#
#   def test_related_articles_filters_by_closest_tags_match(self):
#
#     def create_article_with_tags(*tags):
#       a = Article.objects.create(title='show_article' + str(randint(0, 1000)), body='')
#       a.tags.add(*tags)
#       a.save()
#       return a
#
#     scala_tag  = Tag.objects.create(name='Scala')
#     css_tag    = Tag.objects.create(name='css')
#     refact_tag = Tag.objects.create(name='refactoring')
#     python_tag = Tag.objects.create(name='python')
#     django_tag = Tag.objects.create(name='django')
#     coding_tag = Tag.objects.create(name='coding')
#     cool_tag   = Tag.objects.create(name='cool stuff')
#
#     a = create_article_with_tags(scala_tag, css_tag, refact_tag)
#     b = create_article_with_tags(python_tag, css_tag, django_tag)
#     c = create_article_with_tags(scala_tag, css_tag, refact_tag)
#     d = create_article_with_tags(coding_tag, refact_tag, cool_tag, python_tag, css_tag)
#     e = create_article_with_tags(scala_tag)
#
#     related_articles = a._build_related_list()
#     self.assertEqual(c, related_articles[0]) # Exact tag match
#     self.assertEqual(d, related_articles[1]) # next closes match









