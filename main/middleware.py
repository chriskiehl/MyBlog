

class ConditionalSessionMiddleware(object):
  '''
  Only sends session related headers when in the admin to allow easier proxy caching
  '''
  def process_response(self, request, response):
    if not request.path.startswith('/admin'):
      response._headers.pop('vary')
      response.cookies.pop('csrftoken')
    return response
