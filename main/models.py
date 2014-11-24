from django.db import models


class Article(models.Model):
  title_image = models.CharField(max_length=300)
  title = models.CharField(max_length=300)
  sub_title = models.CharField(max_length=300)
  body = models.TextField()
  pub_date = models.DateTimeField('date published')
  last_modified = models.DateTimeField('last modified')
  views = models.IntegerField(default=0)

  def __unicode__(self):
    return "Article titled: {0}".format(self.title)


# class Comment(models.Model):
#   name = models.CharField(max_length=200)
#   body = models.TextField()
#   pub_date = models.DateTimeField('date posted')
#   article = models.ForeignKey(Article)
#
#   def __unicode__(self):
#     return "Comment by: {0}".format(self.name)
