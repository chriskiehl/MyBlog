# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0018_auto_20150111_1459'),
    ]

    operations = [
        migrations.AddField(
            model_name='article',
            name='related',
            field=models.ManyToManyField(related_name='related_rel_+', to='main.Article'),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='article',
            name='last_modified',
            field=models.DateTimeField(default=datetime.datetime(2015, 1, 11, 15, 48, 44, 413000), verbose_name=b'last modified'),
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2015, 1, 11, 15, 48, 44, 414000)),
        ),
        migrations.AlterField(
            model_name='tag',
            name='name',
            field=models.CharField(unique=True, max_length=30),
        ),
    ]
