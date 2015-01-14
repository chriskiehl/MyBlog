# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0022_auto_20150111_1917'),
    ]

    operations = [
        migrations.AlterField(
            model_name='article',
            name='last_modified',
            field=models.DateTimeField(default=datetime.datetime(2015, 1, 12, 22, 0, 13, 184000), verbose_name=b'last modified'),
        ),
        migrations.AlterField(
            model_name='article',
            name='tags',
            field=models.ManyToManyField(to=b'main.Tag', null=True),
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2015, 1, 12, 22, 0, 13, 186000)),
        ),
    ]
