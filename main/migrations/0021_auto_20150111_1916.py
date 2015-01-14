# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0020_auto_20150111_1910'),
    ]

    operations = [
        migrations.AlterField(
            model_name='article',
            name='last_modified',
            field=models.DateTimeField(default=datetime.datetime(2015, 1, 11, 19, 16, 57, 638000), verbose_name=b'last modified'),
        ),
        migrations.AlterField(
            model_name='article',
            name='related',
            field=models.ManyToManyField(related_name='related_rel_+', null=True, to=b'main.Article'),
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2015, 1, 11, 19, 16, 57, 639000)),
        ),
    ]
