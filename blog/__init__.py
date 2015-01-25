from __future__ import absolute_import

from .celery import app as celery_app


from main.models import Article

Article.content_out_of_date = True


