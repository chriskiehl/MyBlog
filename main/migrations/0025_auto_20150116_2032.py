# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0024_auto_20150115_2105'),
    ]

    operations = [
        migrations.AddField(
            model_name='comment',
            name='admin_comment',
            field=models.BooleanField(default=False),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='article',
            name='last_modified',
            field=models.DateTimeField(default=datetime.datetime(2015, 1, 16, 20, 32, 48, 341000), verbose_name=b'last modified'),
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2015, 1, 16, 20, 32, 48, 343000)),
        ),
    ]
