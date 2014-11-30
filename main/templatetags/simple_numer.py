from django import template
import operator

register = template.Library()

@register.filter
def next_item(value):
  print next(value)

@register.filter
def to(value, arg):
  return range(value, arg)


class SetContextNode(template.Node):
  def __init__(self, var, val):
    self.var = var
    self.val = val

  def render(self, context):
    context[self.var] = self.val
    # a = context.dicts[-1].copy()
    # a[self.var] = self.val
    # context.dicts[-1] = a
    # b =1
    return ''

class BasicAdditionNode(template.Node):
  def __init__(self, tokens, method):
    try:
      tag, var = tokens.split_contents()
      self.var = var
      self.method = method
    except ValueError:
      raise template.TemplateSyntaxError("'set' tag must be of the form:  {% increment <var_name> %}")

  def render(self, context):
    for index, contextDict in enumerate(context.dicts):
      if self.var in contextDict:
        context.dicts[index][self.var] = self.method(int(context[self.var]), 1)
    return ''


@register.tag
def decrement(parser, tokens):
  """ {% decrement <var_name> %} """
  return BasicAdditionNode(tokens, operator.sub)

@register.tag
def increment(parser, tokens):
  """ {% increment <var_name> %} """
  return BasicAdditionNode(tokens, operator.add)

@register.tag
def set_int(parser, token):
  """ {% set <var_name>  = <var_value> %} """
  try:
    action, varname, equals, value = token.split_contents()
  except ValueError:
    raise template.TemplateSyntaxError("'set' tag must be of the form:  {% set <var_name>  = <var_value> %}")
  return SetContextNode(varname, int(value))












