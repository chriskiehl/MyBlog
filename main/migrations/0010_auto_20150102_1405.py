# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0009_auto_20141228_2044'),
    ]

    operations = [
        migrations.AddField(
            model_name='article',
            name='published',
            field=models.BooleanField(default=False),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2015, 1, 2, 14, 5, 55, 581000)),
        ),
    ]
