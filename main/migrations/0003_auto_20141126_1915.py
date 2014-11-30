# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0002_auto_20141123_2019'),
    ]

    operations = [
        migrations.CreateModel(
            name='Comment',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('author', models.CharField(max_length=50)),
                ('body', models.TextField()),
                ('post_date', models.DateField()),
                ('article', models.ForeignKey(to='main.Article')),
                ('parent_id', models.ForeignKey(to='main.Comment')),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.AddField(
            model_name='article',
            name='thumbnail',
            field=models.CharField(default='', max_length=300),
            preserve_default=False,
        ),
    ]
