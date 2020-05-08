#
# Copyright 2020 The Department of Interior
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


def main():
    import os, glob
    import cv2
    import numpy as np
    import pathlib
    import subprocess
    import multiprocessing
    import exiftool
    import datetime
    import re
    import micasense.imageset as imageset
    import micasense.imageutils as imageutils
    import micasense.plotutils as plotutils
    import micasense.capture as capture

    global micasense_batch_warp_mode

    useDLS = True
    overwrite = True # usefult to set to false to continue interrupted processing
    generateThumbnails = True
    micasense_batch_warp_mode = cv2.MOTION_HOMOGRAPHY # MOTION_HOMOGRAPHY or MOTION_AFFINE. For Altum images only use HOMOGRAPHY  TODO : Detect what the images are and set this intelligently based on it

    imagePath = pathlib.Path(os.environ['MICASENSE_IN'])

    panelNames = glob.glob(os.path.join(imagePath,'IMG_0000_*.tif'))

    imageNames = glob.glob(os.path.join(imagePath, '*.*'))

    outputPath = pathlib.Path(os.environ['MICASENSE_OUT'])
    thumbnailPath = os.path.join(outputPath, 'thumbnails')

    alignmentGlob = glob.glob(os.path.join(imagePath,'IMG_0009_*.tif'))

    ###### VALIDATION STAGE ########
    if panelNames is None or len(panelNames) == 0:
        print("Unable to find image panel. This may effect image color quality. Did you include a panel with naming convention IMG_0000_*.tif? (MICA-CODE:1)")
        panelCap = None
    else:
        panelCap = capture.Capture.from_filelist(panelNames)

    if alignmentGlob is None or len(alignmentGlob) == 0:
        raise Exception("Alignment image not found. Did you include an alignment image with naming convention IMG_0001_*.tif? (MICA-CODE:2)")
    else:
        alignmentCapture = capture.Capture.from_filelist(alignmentGlob)

    if panelCap is not None:
        if panelCap.panel_albedo() is not None:
            panel_reflectance_by_band = panelCap.panel_albedo()
            print("Detected panel albedo")
        else:
            panel_reflectance_by_band = [0.67, 0.69, 0.68, 0.61, 0.67] #RedEdge band_index order
            print("Detected RedEdge panel")
        panel_irradiance = panelCap.panel_irradiance(panel_reflectance_by_band)
        img_type = "reflectance"
        #alignmentCapture.plot_undistorted_reflectance(panel_irradiance)
    else:
        if useDLS:
            img_type='reflectance'
            #alignmentCapture.plot_undistorted_reflectance(capture.dls_irradiance())
        else:
            img_type = "radiance"
            #alignmentCapture.plot_undistorted_radiance()
    print("Image type is " + str(img_type))

    captures = []
    generatedOut = []

    # The Key in this hashmap looks like IMG_****_* where the first * represents the image number, and the second star is the band number
    imgGroups = {}

    prog = re.compile('IMG_([0-9]{2,8})_([0-9]{1,3}).*.tif')

    for fullPath in imageNames:

        fileName = os.path.basename(fullPath)

        match = prog.match(fileName)

        if match is None:
            raise Exception("Image [" + fileName + "] does not match the naming convention. Are you following the proper naming convention for your images? (MICA-CODE:3)")

        imageId = match.group(1)
        bandId = match.group(2)

        if not imageId in imgGroups:
            imgGroups[imageId] = []

        bandArr = imgGroups[imageId]

        bandArr.append(fullPath)

    for imageId in imgGroups:
        if imageId != '0000':
            print("Will process imageId: " + imageId)
            captures.append({
                "capture" : capture.Capture.from_filelist(imgGroups[imageId]),
                "imageId" : imageId
                })
        else:
            if len(panelNames) > 0:
                print("Will process 0000 as panel")

    if not os.path.exists(outputPath):
        os.makedirs(outputPath)
    if generateThumbnails and not os.path.exists(thumbnailPath):
        os.makedirs(thumbnailPath)

    try:
        irradiance = panel_irradiance+[0]
    except NameError:
        irradiance = None

    # Our experience shows that once a good alignmnet transformation is found, it tends to be stable over a flight and, in most cases, over many flights.
    # The transformation may change if the camera undergoes a shock event (such as a hard landing or drop) or if the temperature changes substantially between flights.
    # In these events a new transformation may need to be found.
    warp_matrices, alignment_pairs = alignment(alignmentCapture, "0001")

    start = datetime.datetime.now()
    for i,capObj in enumerate(captures):
        cap = capObj["capture"]
        imageId = capObj["imageId"]

        outputFilename = 'IMG_' + imageId + '.tif'
        thumbnailFilename = 'IMG_' + imageId + '.jpg'

        fullOutputPath = os.path.join(outputPath, outputFilename)
        fullThumbnailPath= os.path.join(thumbnailPath, thumbnailFilename)

        generatedOut.append(fullOutputPath)
        generatedOut.append(fullThumbnailPath)

        if (not os.path.exists(fullOutputPath)) or overwrite:
            cap.create_aligned_capture(irradiance_list=irradiance, warp_matrices=warp_matrices, normalize=False) # TODO : motion_type=micasense_batch_warp_mode
            cap.save_capture_as_stack(fullOutputPath)
            if generateThumbnails:
                cap.save_capture_as_rgb(fullThumbnailPath)
        cap.clear_image_data()
    end = datetime.datetime.now()

    print("Saving time: {}".format(end-start))
    print("Alignment+Saving rate: {:.2f} images per second".format(float(len(captures))/float((end-start).total_seconds())))


    #### Image Metadata (EXIF) #####

    def decdeg2dms(dd):
       is_positive = dd >= 0
       dd = abs(dd)
       minutes,seconds = divmod(dd*3600,60)
       degrees,minutes = divmod(minutes,60)
       degrees = degrees if is_positive else -degrees
       return (degrees,minutes,seconds)

    header = "SourceFile,\
    GPSDateStamp,GPSTimeStamp,\
    GPSLatitude,GpsLatitudeRef,\
    GPSLongitude,GPSLongitudeRef,\
    GPSAltitude,GPSAltitudeRef,\
    FocalLength,\
    XResolution,YResolution,ResolutionUnits\n"

    lines = [header]
    for capObj in captures:
        cap = capObj["capture"]
        imageId = capObj["imageId"]

        #get lat,lon,alt,time
        outputFilename = 'IMG_' + imageId + '.tif'
        fullOutputPath = os.path.join(outputPath, outputFilename)
        thumbnailOutputFilename = 'IMG_' + imageId + '.jpg'
        thumbnailFullOutputPath = os.path.join(outputPath, "thumbnails/" + thumbnailOutputFilename)
        lat,lon,alt = cap.location()
        #write to csv in format:
        # IMG_0199_1.tif,"33 deg 32' 9.73"" N","111 deg 51' 1.41"" W",526 m Above Sea Level
        latdeg, latmin, latsec = decdeg2dms(lat)
        londeg, lonmin, lonsec = decdeg2dms(lon)
        latdir = 'North'
        if latdeg < 0:
            latdeg = -latdeg
            latdir = 'South'
        londir = 'East'
        if londeg < 0:
            londeg = -londeg
            londir = 'West'
        resolution = cap.images[0].focal_plane_resolution_px_per_mm

        linestr = '"{}",'.format(fullOutputPath)
        linestr += cap.utc_time().strftime("%Y:%m:%d,%H:%M:%S,")
        linestr += '"{:d} deg {:d}\' {:.2f}"" {}",{},'.format(int(latdeg),int(latmin),latsec,latdir[0],latdir)
        linestr += '"{:d} deg {:d}\' {:.2f}"" {}",{},{:.1f} m Above Sea Level,Above Sea Level,'.format(int(londeg),int(lonmin),lonsec,londir[0],londir,alt)
        linestr += '{}'.format(cap.images[0].focal_length)
        linestr += '{},{},mm'.format(resolution,resolution)
        linestr += '\n' # when writing in text mode, the write command will convert to os.linesep
        lines.append(linestr)

        linestr = '"{}",'.format(thumbnailFullOutputPath)
        linestr += cap.utc_time().strftime("%Y:%m:%d,%H:%M:%S,")
        linestr += '"{:d} deg {:d}\' {:.2f}"" {}",{},'.format(int(latdeg),int(latmin),latsec,latdir[0],latdir)
        linestr += '"{:d} deg {:d}\' {:.2f}"" {}",{},{:.1f} m Above Sea Level,Above Sea Level,'.format(int(londeg),int(lonmin),lonsec,londir[0],londir,alt)
        linestr += '{}'.format(cap.images[0].focal_length)
        linestr += '{},{},mm'.format(resolution,resolution)
        linestr += '\n' # when writing in text mode, the write command will convert to os.linesep
        lines.append(linestr)

    fullCsvPath = os.path.join(outputPath,'log.csv')
    with open(fullCsvPath, 'w') as csvfile: #create CSV
        csvfile.writelines(lines)

    old_dir = os.getcwd()
    os.chdir(outputPath)
    cmd = 'exiftool -csv="{}" -overwrite_original -r .'.format(fullCsvPath)
    print(cmd)
    try:
        subprocess.run(cmd, check=True, shell=True)
    finally:
        os.chdir(old_dir)


    print("Micasense Batch Processing Completed Successfully. The following products were generated:")
    for i,filename in enumerate(generatedOut):
        print(str(filename))


def alignment(cap, imageId):
    import micasense.capture as capture
    import cv2
    import micasense.imageutils as imageutils
    import multiprocessing

    reference_band_index = 0
    warp_matrices = None
    while warp_matrices == None:
        try:
            ## Alignment settings
            max_alignment_iterations = 50
            pyramid_levels = None # for images with RigRelatives, setting this to 0 or 1 may improve alignment

            print("Aligning capture " + str(imageId) + " images with reference index " + str(reference_band_index) + ". Depending on settings this can take from a few seconds to many minutes")
            # Can potentially increase max_iterations for better results, but longer runtimes
            warp_matrices, alignment_pairs = imageutils.align_capture(cap,
                                                                      ref_index = reference_band_index,
                                                                      max_iterations = max_alignment_iterations,
                                                                      warp_mode = micasense_batch_warp_mode,
                                                                      pyramid_levels = pyramid_levels)
        except Exception as ex:
            if reference_band_index+1 < 3:
                print ("Alignment with reference band " + str(reference_band_index) + " failed. Trying the next band.")
            else:
                print ("Alignment with reference band " + str(reference_band_index) + " failed.")
                raise Exception("Image alignment has failed. (MICA-CODE:4)")

            reference_band_index = reference_band_index + 1

    print("Finished Aligning, warp matrices={}".format(warp_matrices))

    return warp_matrices, alignment_pairs

if __name__ == '__main__':
    main()
