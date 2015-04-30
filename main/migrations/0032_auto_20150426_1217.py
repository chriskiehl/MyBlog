# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0031_auto_20150124_1813'),
    ]

    operations = [
        migrations.AlterModelOptions(
            name='article',
            options={'ordering': ('last_modified',)},
        ),
        migrations.AddField(
            model_name='article',
            name='working_draft',
            field=models.TextField(null=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='article',
            name='last_modified',
            field=models.DateTimeField(default=datetime.datetime(2015, 4, 26, 12, 17, 29, 642000), verbose_name=b'last modified'),
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2015, 4, 26, 12, 17, 29, 644000)),
        ),
    ]
