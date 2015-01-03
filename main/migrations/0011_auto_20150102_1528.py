# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0010_auto_20150102_1405'),
    ]

    operations = [
        migrations.AlterField(
            model_name='article',
            name='pub_date',
            field=models.DateTimeField(null=True, verbose_name=b'date published'),
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2015, 1, 2, 15, 28, 20, 560000)),
        ),
    ]
