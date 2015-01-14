# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0017_auto_20150111_1129'),
    ]

    operations = [
        migrations.CreateModel(
            name='Tag',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('name', models.CharField(max_length=30)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.AddField(
            model_name='article',
            name='tags',
            field=models.ManyToManyField(to='main.Tag'),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='article',
            name='last_modified',
            field=models.DateTimeField(default=datetime.datetime(2015, 1, 11, 14, 59, 51, 563000), verbose_name=b'last modified'),
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2015, 1, 11, 14, 59, 51, 565000)),
        ),
    ]
