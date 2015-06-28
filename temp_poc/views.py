from django.shortcuts import render


def index(request):
  return render(request, 'temp_poc/index.html', {})
