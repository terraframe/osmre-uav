
import os, glob
import pathlib
import re

# The Key in this hashmap looks like IMG_****_* where the first * represents the image number, and the second star is the band number
imgGroups = {}

imagePath = pathlib.Path(os.environ['MICASENSE_IN'])
imageNames = glob.glob(os.path.join(imagePath, '*.*'))

prog = re.compile('IMG_([0-9]{2,8})_([0-9]{1,3}).tif')

for fullPath in imageNames:

    fileName = os.path.basename(fullPath)

    match = prog.match(fileName)

    imageId = match.group(1)
    bandId = match.group(2)

    if not imageId in imgGroups:
        imgGroups[imageId] = []

    bandArr = imgGroups[imageId]

    bandArr.append(fullPath)

print(imgGroups)
