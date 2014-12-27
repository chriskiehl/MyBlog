# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0007_auto_20141220_2206'),
    ]

    operations = [
        migrations.AddField(
            model_name='article',
            name='should_display_comments',
            field=models.BooleanField(default=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2014, 12, 26, 22, 59, 15, 202000)),
        ),
    ]
