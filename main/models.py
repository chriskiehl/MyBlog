from datetime import datetime
from itertools import chain
from django.db import models
from django.db.models import QuerySet


class Article(models.Model):
  title_image = models.CharField(max_length=300)
  thumbnail = models.CharField(max_length=300)
  title = models.CharField(max_length=300)
  slug = models.CharField(max_length=100)
  sub_title = models.CharField(max_length=300)
  body = models.TextField()
  pub_date = models.DateTimeField('date published')
  last_modified = models.DateTimeField('last modified')
  views = models.IntegerField(default=0)

  def __unicode__(self):
    return "Article titled: {0}".format(self.title)

class CommentManager(models.Manager):
  def get_queryset(self):
    return self.model.MyQuerySet(self.model)

class Node(object):
  def __init__(self, comment):
    self.comment = comment
    self.id = comment.id
    self.pid = comment.parent_id.id if comment.parent_id else None
    self.children = []

    def __str__(self):
      return self.comment.author

    def __repr__(self):
      return self.__str__()


class Comment(models.Model):
  article = models.ForeignKey('Article')
  parent_id = models.ForeignKey('Comment', null=True)
  author = models.CharField(max_length=50)
  body = models.TextField()
  post_date = models.DateField(default=datetime.now())
  deleted = models.BooleanField(default=False)

  objects = CommentManager()

  def __unicode__(self):
    return "Comment by: {0}".format(self.author)

  def __repr__(self):
    return self.__unicode__()

  @classmethod
  def create(cls, article, parent_id, author, body):
    return cls(
      article=article,
      parent_id=parent_id,
      author=author,
      body=body,
    )

  class MyQuerySet(QuerySet):

    def as_tree(self):
      comment_nodes = [Node(comment) for comment in self]
      lookup_table = {node.id: node for node in comment_nodes}

      # populate children
      for node in comment_nodes:
        current = node
        try:
          lookup_table[current.pid].children.append(current)
        except:
          pass

      fake_root_comment = Comment(article=comment_nodes[0].comment.article, parent_id=None, author="", body="")
      root_id = 9999294999
      root_node = Node(fake_root_comment)
      for node in comment_nodes:
        if not node.pid:
          root_node.children.append(node)

      return self.build_tree(root_node, 0)

    def build_tree(self, node, depth):
      if not node.children:
        yield (depth, node.comment,)
      else:
        output = [(depth, node.comment,)]
        for n in node.children:
          output = chain(output, self.build_tree(n, depth + 1))
        for result in output:
          yield result












