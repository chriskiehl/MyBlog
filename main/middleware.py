import time
from django.utils.cache import cc_delim_re


class VaryStripper(object):

  def process_response(self, request, response):
    print request.path
    if '/admin/' not in request.path and response.has_header('Vary'):
        del response['Vary']
    return response
