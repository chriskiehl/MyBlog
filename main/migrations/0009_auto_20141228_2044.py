# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0008_auto_20141226_2259'),
    ]

    operations = [
        migrations.RenameField(
            model_name='comment',
            old_name='parent_id',
            new_name='parent',
        ),
        migrations.AlterField(
            model_name='comment',
            name='post_date',
            field=models.DateField(default=datetime.datetime(2014, 12, 28, 20, 44, 3, 77000)),
        ),
    ]
