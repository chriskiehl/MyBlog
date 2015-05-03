import pickle
import urllib2
from PIL import Image, ImageFilter
import cStringIO
from django.conf import settings


def create_thumbnail(source_image):
  target_width = settings.THUMBNAIL_WIDTH
  target_height = settings.THUMBNAIL_HEIGHT
  im = Image.open(cStringIO.StringIO(urllib2.urlopen(source_image).read()))
  aspect_ratio = calc_aspect_ratio(im)

  new_width = int(target_height * aspect_ratio)
  resized_im = im.resize((new_width, target_height), Image.ANTIALIAS)
  cropped_im = crop_center(resized_im, (target_width, target_height))
  shapened_im = cropped_im.filter(ImageFilter.UnsharpMask(percent=100))

  output = cStringIO.StringIO()
  shapened_im.save(output, 'jpeg')
  output.seek(0)
  return output


def calc_aspect_ratio(image):
  w, h = image.size
  return float(w)/h

def crop_center(im, target_size):
  target_width, target_height = target_size
  current_width, current_height  = im.size
  left = (current_width / 2) - (target_width / 2)
  bottom = (current_width / 2) + (target_width / 2)
  top = (current_height / 2) - (target_height / 2)
  right = (current_height / 2) + (target_height / 2)
  return im.crop((left, top, right, bottom))


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

def populate_db():
  # creates a couple of fake articles to play around with
  from main.models import Article
  titles = [
    'Ones Upon a Time',
    'Twice upon a time',
    'Pice upon a time',
    'Frice upon a time',
    'Mice upon a time',
    'Guize upon a time',
  ]
  for title in titles:
    Article.objects.create(
      title_image='https://awsblogstore.s3.amazonaws.com/main/images/hahahahaha.jpg',
      thumbnail='https://awsblogstore.s3.amazonaws.com/main/images/hahahahaha-thumb.jpg',
      slug=title.split(' ')[0],
      title=title,
      sub_title='This is a cool subtitle',
      body='Lorem ipsum dolor sit amet, ad ius possit mentitum epicurei, mei exerci pertinax interesset cu. Ex mea dico unum definiebas. Sea essent itellegat eu, alii quot hendrerit eu vix. Nisl persius sapientem nam ex, te eos oblique molestiae, te has persius habemus delectus. Id vis lorem homero. Vel in soleat doming copiosae. Ea dico quodsi maluisset eos. Ea pri alii pericula temporibus, per atqui velit te. Nemore corpora ne eum, cum an utamur mnesarchum, mel et quodsi debitis. Id invidunt recteque reprehendunt sed. Eu cum erant oporteat. Solet saepe everti ei vim, qui malorum fastidii cotidieque an, his et illud molestie. Nam accusam lucilius ad. An mea tempor blandit.',
      working_copy='',
      published=True
    )
