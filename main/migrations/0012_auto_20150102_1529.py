# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0011_auto_20150102_1528'),
    ]

    operations = [
        migrations.AlterField(
            model_name='article',
            name='last_modified',
            field=models.DateTimeField(default=datetime.datetime(2015, 1, 2, 15, 29, 3, 796000), verbose_name=b'last modified'),
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2015, 1, 2, 15, 29, 3, 796000)),
        ),
    ]
