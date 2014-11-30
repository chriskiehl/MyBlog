# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0005_auto_20141126_1940'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='comment',
            name='body_html',
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2014, 11, 28, 12, 53, 27, 663000)),
        ),
    ]
