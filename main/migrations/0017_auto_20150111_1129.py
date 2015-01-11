# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0016_auto_20150104_1530'),
    ]

    operations = [
        migrations.AlterField(
            model_name='article',
            name='body',
            field=models.TextField(null=True),
        ),
        migrations.AlterField(
            model_name='article',
            name='last_modified',
            field=models.DateTimeField(default=datetime.datetime(2015, 1, 11, 11, 29, 56, 127000), verbose_name=b'last modified'),
        ),
        migrations.AlterField(
            model_name='article',
            name='sub_title',
            field=models.CharField(max_length=300, null=True),
        ),
        migrations.AlterField(
            model_name='article',
            name='thumbnail',
            field=models.CharField(max_length=300, null=True),
        ),
        migrations.AlterField(
            model_name='article',
            name='title_image',
            field=models.CharField(max_length=300, null=True),
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2015, 1, 11, 11, 29, 56, 128000)),
        ),
    ]
