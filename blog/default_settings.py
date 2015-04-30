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
SECRET_KEY = '5b@p5_7gw=(9mmtcyn%-iqf8vpe@b5ev0@z*p4#@c)efwjv9vb'

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
    'rest_framework',
)

MIDDLEWARE_CLASSES = (
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

  if 'test' in sys.argv:
    DATABASES['default'] = {
      'ENGINE': 'django.db.backends.sqlite3',
      'NAME': 'mytestdatabase'
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

  # # running against memcached in virtual box
  # CACHES = {
  #     'default': {
  #         'BACKEND': 'django.core.cache.backends.memcached.MemcachedCache',
  #         'LOCATION': '192.168.1.9:11211',
  #         'KEY_PREFIX': "HelloWorld"
  #     }
  # }

SESSION_ENGINE = 'django.contrib.sessions.backends.cache'


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

AWS_ACCESS_KEY_ID = 'AKIAIIYFFFOR2KL2MRUQ'
AWS_SECRET_ACCESS_KEY = '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'
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
EMAIL_HOST_PASSWORD = '6a4602fc656bfc0152e1d62393a190b4'
EMAIL_PORT = 587
EMAIL_USE_TLS = True

ADMINS = ('kiehlbot@gmail.com',)

REST_FRAMEWORK = {
    # Use Django's standard `django.contrib.auth` permissions,
    # or allow read-only access for unauthenticated users.
    'DEFAULT_PERMISSION_CLASSES': [
        'rest_framework.permissions.DjangoModelPermissionsOrAnonReadOnly'
    ]
}

