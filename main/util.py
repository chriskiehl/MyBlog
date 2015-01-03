import urllib2
from PIL import Image, ImageFilter
import cStringIO


def create_thumbnail(source_image):
  target_width, target_height = (212, 119)
  im = Image.open(cStringIO.StringIO(urllib2.urlopen(source_image).read()))
  source_width, source_height = im.size
  aspect_ratio = float(source_width) / source_height

  new_width = int(target_height * aspect_ratio)
  resized_im = im.resize((new_width, target_height), Image.ANTIALIAS)
  print 'Resized: ', resized_im.size
  resized_width  = resized_im.size[0]
  crop_loc_left  = (resized_width / 2) - (target_width / 2)
  crop_loc_right = (resized_width / 2) + (target_width / 2)

  cropped_im = resized_im.crop((crop_loc_left, 0, crop_loc_right, target_height))
  print cropped_im.size
  shapened_img = cropped_im.filter(ImageFilter.UnsharpMask(percent=120))

  output = cStringIO.StringIO()
  shapened_img.save(output, 'png')
  output.seek(0)
  return output

if __name__ == '__main__':
  im = create_thumbnail('https://s3.amazonaws.com/awsblogstore/main/images/place_holder.png')
  im.save("adsfasdfasdfasdfadsf1.png", "png")


