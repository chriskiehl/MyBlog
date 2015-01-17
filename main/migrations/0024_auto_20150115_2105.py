# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0023_auto_20150112_2200'),
    ]

    operations = [
        migrations.AddField(
            model_name='article',
            name='generate_related',
            field=models.BooleanField(default=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='article',
            name='last_modified',
            field=models.DateTimeField(default=datetime.datetime(2015, 1, 15, 21, 5, 19, 586000), verbose_name=b'last modified'),
        ),
        migrations.AlterField(
            model_name='article',
            name='pub_date',
            field=models.DateTimeField(null=True, verbose_name=b'date published', blank=True),
        ),
        migrations.AlterField(
            model_name='article',
            name='related',
            field=models.ManyToManyField(related_name='related_rel_+', null=True, to=b'main.Article', blank=True),
        ),
        migrations.AlterField(
            model_name='article',
            name='slug',
            field=models.CharField(max_length=100, blank=True),
        ),
        migrations.AlterField(
            model_name='article',
            name='sub_title',
            field=models.CharField(max_length=300, null=True, blank=True),
        ),
        migrations.AlterField(
            model_name='article',
            name='tags',
            field=models.ManyToManyField(to=b'main.Tag', null=True, blank=True),
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2015, 1, 15, 21, 5, 19, 588000)),
        ),
    ]
