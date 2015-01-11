import json
from django.core.urlresolvers import reverse
from django.test import TestCase

# Create your tests here.
from main.models import Article, Comment


class TestAdminAjaxSave(TestCase):

  def setUp(self):
    Article.objects.create(title='cool article', slug='cool-article')
    self.url = reverse('comment_store', kwargs={'slug': 'cool-article'})
    self.data = {
      'body': 'hello!',
      'name': 'myname',
      'parent_id': '123423543456'
    }


  def ajax_post(self, url, data=None):
    return self.client.post(url, data=data, HTTP_X_REQUESTED_WITH='XMLHttpRequest')


  def test_storecomment_rejects_nonajax_requests(self):
    response = self.client.get(self.url)
    self.assertEqual(400, response.status_code)


  def test_comment_ajax_fails_for_non_post_request(self):
    responses = (
      self.client.get(self.url, HTTP_X_REQUESTED_WITH='XMLHttpRequest'),
      self.client.put(self.url, HTTP_X_REQUESTED_WITH='XMLHttpRequest'),
      self.client.delete(self.url, HTTP_X_REQUESTED_WITH='XMLHttpRequest'),
    )

    for response in responses:
      self.assertEqual(400, response.status_code)


  def test_storecomment_returns_error_on_missing_params(self):
    response = self.ajax_post(self.url)
    self.assertTrue(response.status_code == 400)
    msg = json.loads(response.content)

    missing_params = ('body', 'parent_id', 'name')
    for param in missing_params:
      self.assertTrue(param in msg.get('error'))

    response2 = json.loads(self.ajax_post(self.url, data={'body': 'hello'}).content)
    # body should now *not* be part of the missing params response
    self.assertTrue('body' not in response2.get('error'))


  def test_storecomment_returns_error_on_no_matching_article(self):
    bad_url = reverse('comment_store', kwargs={'slug': 'does-not-exist'})
    response = json.loads(self.ajax_post(bad_url, data=self.data).content)
    self.assertEqual('No matching article found', response.get('error'))


  def test_storecomment_saves_on_valid_params(self):
    self.assertFalse(Comment.objects.filter(body='hello!').exists())
    self.ajax_post(self.url, data=self.data)
    self.assertTrue(Comment.objects.filter(body='hello!').exists())
