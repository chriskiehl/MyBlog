from random import randint
import sys, os
import csv
from datetime import datetime


sys.path.append('../blog')
os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'
import django
django.setup()
from main.models import Article, Comment


if __name__ == "__main__":
  os.environ.setdefault("DJANGO_SETTINGS_MODULE", "blog.settings")
  # for i in Comment.objects.all():
  #   print i.id, i.parent_id.id if i.parent_id is not None else "None", i.author
  with open('../other/MOCK_DATA.csv') as f:
    csvreader = csv.reader(f, delimiter=",")
    data = [line for line in csvreader]

  article = Article.objects.get(id=1)
  for i in range(20):
    c = Comment(article=article,
                parent_id=None,
                author=data[i][0],
                body=data[i][1])
    c.save()

  parents = list(Comment.objects.all())
  article = Article.objects.get(id=1)
  for username, body in data:
    pid = parents[randint(0, len(parents) - 2)]
    c = Comment(article=article,
                parent_id=pid,
                author=username,
                body=body)
    print 'Adding comment with parent id:', pid.id
    c.save()
    parents.append(c)
