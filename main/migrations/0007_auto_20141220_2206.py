# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0006_auto_20141128_1253'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='comment',
            name='edited',
        ),
        migrations.AddField(
            model_name='article',
            name='slug',
            field=models.CharField(default='Nothing', max_length=100),
            preserve_default=False,
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2014, 12, 20, 22, 6, 30, 298000)),
        ),
    ]
