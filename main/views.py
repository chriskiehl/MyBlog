from django.http.response import HttpResponse
from django.shortcuts import render


def reader():
    """A generator that fakes a read from a file, socket, etc."""
    for i in range(4):
        yield '<< %s' % i

def reader_wrapper(g):
    # Manually iterate over data produced by reader
    return [1,2]

def index(request):
  title = "Hello world!"
  return render(request, 'main/index.html', locals())
