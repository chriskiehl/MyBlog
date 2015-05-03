# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Article',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('title_image', models.CharField(max_length=300, null=True, blank=True)),
                ('thumbnail', models.CharField(max_length=300, null=True)),
                ('slug', models.CharField(max_length=100, blank=True)),
                ('title', models.CharField(max_length=300)),
                ('sub_title', models.CharField(max_length=300, null=True, blank=True)),
                ('body', models.TextField(null=True)),
                ('working_copy', models.TextField(null=True)),
                ('pub_date', models.DateTimeField(null=True, verbose_name=b'date published', blank=True)),
                ('last_modified', models.DateTimeField(default=datetime.datetime(2015, 5, 2, 16, 47, 50, 40000), verbose_name=b'last modified')),
                ('views', models.IntegerField(default=0)),
                ('should_display_comments', models.BooleanField(default=True)),
                ('published', models.BooleanField(default=False)),
                ('generate_related', models.BooleanField(default=True)),
                ('stale', models.BooleanField(default=False)),
                ('related', models.ManyToManyField(related_name='related_rel_+', to='main.Article', blank=True)),
            ],
        ),
        migrations.CreateModel(
            name='Tag',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('name', models.CharField(unique=True, max_length=30)),
            ],
        ),
        migrations.AddField(
            model_name='article',
            name='tags',
            field=models.ManyToManyField(to='main.Tag', blank=True),
        ),
    ]
