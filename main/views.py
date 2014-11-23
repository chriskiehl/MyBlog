from django.http.response import HttpResponse
from django.shortcuts import render


def reader():
    """A generator that fakes a read from a file, socket, etc."""
    for i in range(4):
        yield '<< %s' % i

def reader_wrapper(g):
    # Manually iterate over data produced by reader
    yield from g

def index(request):
  wrap = reader_wrapper(reader())

  out = '</h1>Hello world!</h1><p>This Python3 stuff working yet?</p>'
  for i in wrap:
      out += "<p>{0}</p>".format(i)

  return HttpResponse(out)
