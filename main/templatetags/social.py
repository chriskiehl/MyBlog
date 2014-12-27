from django import template
from main.models import Article

register = template.Library()

class TwitterNode(template.Node):
  def __init__(self):
    pass

  def render(self, context):

    article = context.get('article', )

@register.filter
def as_twitter_link(article):
  import urllib
  params = {
    'text': article.title,
    'url': 'http://chriskiehl.com/article/{slug}'.format(slug=article.slug)
  }
  url = 'https://twitter.com/intent/tweet?' + urllib.urlencode(params)
  if len(url) > 120:
    over = len(url) - 120
    params['text']


