import time


class StackTimer(object):

  timer = None
  recording = False

  def __init__(self):
    pass

  def process_request(self, request):
    if self.timer == None:
      # first time its been called. Init the timer
      StackTimer.timer = time.time()
      StackTimer.recording = True
    elif self.recording:
      print 'Time taken in Middleware: {0}'.format(time.time() - StackTimer.timer)
      StackTimer.recording = not StackTimer.recording
    else:
      StackTimer.timer = time.time()
      StackTimer.recording = not StackTimer.recording
