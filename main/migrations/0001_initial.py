# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Article',
            fields=[
                ('id', models.AutoField(auto_created=True, serialize=False, primary_key=True, verbose_name='ID')),
                ('title_image', models.CharField(max_length=300)),
                ('title', models.CharField(max_length=300)),
                ('sub_title', models.CharField(max_length=300)),
                ('body', models.TextField()),
                ('pub_date', models.DateTimeField(verbose_name='date published')),
                ('last_modified', models.DateTimeField(verbose_name='date modified')),
                ('views', models.IntegerField(default=0)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
    ]
