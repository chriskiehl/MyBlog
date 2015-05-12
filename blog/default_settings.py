"""
Django settings for blog project.

For more information on this file, see
https://docs.djangoproject.com/en/1.7/topics/settings/

For the full list of settings and their values, see
https://docs.djangoproject.com/en/1.7/ref/settings/
"""

# Build paths inside the project like this: os.path.join(BASE_DIR, ...)
import os
import sys

BASE_DIR = os.path.dirname(os.path.dirname(__file__))

# Quick-start development settings - unsuitable for production
# See https://docs.djangoproject.com/en/1.7/howto/deployment/checklist/

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = os.getenv('DJANGO_KEY')

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = True

TEMPLATE_DEBUG = True

ALLOWED_HOSTS = ['54.148.72.44', '127.0.0.1', '104.236.70.78', 'chriskiehl.com', 'www.chriskiehl.com']


# Application definition

INSTALLED_APPS = (
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'debug_toolbar',
    'blog',
    'main',
    'storages',
    'rest_framework'
)

MIDDLEWARE_CLASSES = (
    # 'main.middleware.ConditionalSessionMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.auth.middleware.SessionAuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
)

ROOT_URLCONF = 'blog.urls'

WSGI_APPLICATION = 'blog.wsgi.application'


DATABASES = {
  'default': {
      'ENGINE': 'django.db.backends.postgresql_psycopg2',
      'NAME': 'blogdb',
      'USER': 'django',
      'PASSWORD': 'l6O8Pnnn58',
      'HOST': '127.0.0.1'
  }
}

if DEBUG:
  DATABASES = {
      'default': {
          'ENGINE': 'django.db.backends.postgresql_psycopg2',
          'NAME': 'blogdb',
          'USER': 'myuser',
          'PASSWORD': 'password',
          'HOST': ''
      }
  }

CACHES = {
  'default': {
      'BACKEND': 'django.core.cache.backends.memcached.MemcachedCache',
      'LOCATION': '127.0.0.1:11211',
      'KEY_PREFIX': "blogomatono"
  }
}

if DEBUG:
  CACHES = {
    'default': {
          'BACKEND': 'django.core.cache.backends.locmem.LocMemCache',
          'LOCATION': 'unique-snowflake'
    }
  }

SESSION_ENGINE = 'django.contrib.sessions.backends.cached_db'

SESSION_COOKIE_HTTPONLY = False

# Internationalization
# https://docs.djangoproject.com/en/1.7/topics/i18n/

LANGUAGE_CODE = 'en-us'
TIME_ZONE = 'UTC'

USE_I18N = True

USE_L10N = True

USE_TZ = True



TEMPLATE_DIRS = tuple(os.path.join(BASE_DIR, app_name, 'templates') for app_name in INSTALLED_APPS)


# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/1.7/howto/static-files/

STATIC_URL = '/static/'

STATIC_ROOT = '/var/www/blog/static'

STATICFILES_DIRS = (
    os.path.join(BASE_DIR, 'main/static'),
)


CORS_ALLOW_CREDENTIALS = True

AWS_ACCESS_KEY_ID = os.getenv('AWS_ACCESS_KEY_ID')
AWS_SECRET_ACCESS_KEY = os.getenv('AWS_SECRET_ACCESS_KEY')
AWS_STORAGE_BUCKET_NAME = 'awsblogstore'
AWS_CALLING_FORMAT = 'http://{0}.s3.amazonaws.com/'.format(AWS_STORAGE_BUCKET_NAME)

AWS_QUERYSTRING_AUTH = False

AWS_REDUCED_REDUNDANCY = True 

AWS_HEADERS = {
  'Cache-Control': 'max-age=31556926',
}

AWS_IS_GZIPPED = True

if not DEBUG:
  DEFAULT_FILE_STORAGE = 'storages.backends.s3boto.S3BotoStorage'
  STATICFILES_STORAGE = 'storages.backends.s3boto.S3BotoStorage'
  STATIC_URL = AWS_CALLING_FORMAT



EMAIL_HOST = 'smtp.gmail.com'
EMAIL_HOST_USER = 'kiehlbot@gmail.com'
EMAIL_HOST_PASSWORD = os.getenv('EMAIL_BOT_PASSWORD')
EMAIL_PORT = 587
EMAIL_USE_TLS = True

ADMINS = ('kiehlbot@gmail.com',)


THUMBNAIL_WIDTH = 212
THUMBNAIL_HEIGHT = 119

