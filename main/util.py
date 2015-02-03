import pickle
import urllib2
from PIL import Image, ImageFilter
import cStringIO
from django.core.mail import send_mail


def create_thumbnail(source_image):
  target_width, target_height = (212, 119)
  im = Image.open(cStringIO.StringIO(urllib2.urlopen(source_image).read()))
  source_width, source_height = im.size
  aspect_ratio = float(source_width) / source_height

  new_width = int(target_height * aspect_ratio)
  resized_im = im.resize((new_width, target_height), Image.ANTIALIAS)
  print 'Resized: ', resized_im.size
  resized_width  = resized_im.size[0]
  crop_loc_left  = (resized_width / 2) - (target_width / 2)
  crop_loc_right = (resized_width / 2) + (target_width / 2)

  cropped_im = resized_im.crop((crop_loc_left, 0, crop_loc_right, target_height))
  print cropped_im.size
  shapened_img = cropped_im.filter(ImageFilter.UnsharpMask(percent=100))

  output = cStringIO.StringIO()
  shapened_img.save(output, 'jpeg')
  output.seek(0)
  return output


def get_object_or_none(cls, **kwargs):
  try:
    return cls.objects.get(**kwargs)
  except cls.DoesNotExist:
    return None

def build_cache_key(*args, **kwargs):
  cache_key = '-'.join(args) + '-'.join(kwargs.values())
  return cache_key or 'home'


def pickle_safe(d):
  out = {}
  for k, v in d.items():
    try:
      pickle.dumps(v)
      out[k] = v
    except:
      pass
  return out


def pickle_request(request):
  required_req_attributes = ('COOKIES', 'FILES', 'GET', 'META', 'POST', 'REQUEST')
  output = {}
  for k, v in request.__dict__.items():
    if isinstance(v, dict):
      output[k] = pickle_safe(v)
    else:
      try:
        pickle.dumps(v)
        output[k] = v
      except:
        pass
  for attrib in required_req_attributes:
    if attrib not in output:
      output[attrib] = {}
  return output



def format_comment_email(request):
  author, body = request.POST['name'], request.POST['body']
  return '''
  New Comment on {0}.

  Author: {1}
  Body:
  >>> {2}
  '''.format(request.build_absolute_uri(), author, body)


