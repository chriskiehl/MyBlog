import urllib
from django import template
from django.core.urlresolvers import reverse
from django.utils.datastructures import SortedDict
from main.models import Article

register = template.Library()

elipse_padding = 3

class TwitterNode(template.Node):
  def __init__(self):
    pass

  def render(self, context):

    article = context.get('article', )

@register.simple_tag(takes_context=True, name="twitter_share")
def as_twitter_link(context):
  request = context['request']
  return 'https://twitter.com/intent/tweet?' + urllib.urlencode({'url': request.build_absolute_uri()})

@register.simple_tag(takes_context=True, name="google_share")
def google(context):
  request = context['request']
  return 'https://plus.google.com/share?' + urllib.urlencode({'url': request.build_absolute_uri()})


@register.simple_tag(takes_context=True, name="facebook_share")
def book_of_face(context):
  request = context['request']
  return 'https://www.facebook.com/sharer/sharer.php?' + urllib.urlencode({'u': request.build_absolute_uri()})
