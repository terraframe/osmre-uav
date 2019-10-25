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
            max_alignment_iterations = 10
            warp_mode = cv2.MOTION_HOMOGRAPHY # MOTION_HOMOGRAPHY or MOTION_AFFINE. For Altum images only use HOMOGRAPHY
            pyramid_levels = 0 # for images with RigRelatives, setting this to 0 or 1 may improve alignment

            print("Aligning capture " + str(imageId) + " images with reference index " + str(reference_band_index) + ". Depending on settings this can take from a few seconds to many minutes")
            # Can potentially increase max_iterations for better results, but longer runtimes
            warp_matrices, alignment_pairs = imageutils.align_capture(cap,
                                                                      ref_index = reference_band_index,
                                                                      max_iterations = max_alignment_iterations,
                                                                      warp_mode = warp_mode,
                                                                      pyramid_levels = pyramid_levels)
        except cv2.error as ex:
            if reference_band_index+1 < 3:
                print ("Alignment with reference band " + str(reference_band_index) + " failed. Trying the next band.")
            else:
                print ("Alignment with reference band " + str(reference_band_index) + " failed.")
                raise ex

            reference_band_index = reference_band_index + 1

    print("Finished Aligning, warp matrices={}".format(warp_matrices))

    return warp_matrices, alignment_pairs


def main():
    import os, glob
    import micasense.capture as capture
    import pathlib

    panelNames = None

    # This is an altum image with RigRelatives and a thermal band
    imagePath = pathlib.Path(os.environ['MICASENSE_IN'])
    imageNames = glob.glob(os.path.join(imagePath,'IMG_0008_*.tif'))
    panelNames = glob.glob(os.path.join(imagePath,'IMG_0000_*.tif'))

    if panelNames is not None:
        panelCap = capture.Capture.from_filelist(panelNames)
    else:
        panelCap = None

    capture = capture.Capture.from_filelist(imageNames)

    #has_rig_relatives = False
    # for img in capture.images:
    #     if img.rig_relatives is not None:
    #         has_rig_relatives = True

    if panelCap is not None:
        if panelCap.panel_albedo() is not None:
            panel_reflectance_by_band = panelCap.panel_albedo()
        else:
            panel_reflectance_by_band = [0.67, 0.69, 0.68, 0.61, 0.67] #RedEdge band_index order
        panel_irradiance = panelCap.panel_irradiance(panel_reflectance_by_band)
        img_type = "reflectance"
        capture.plot_undistorted_reflectance(panel_irradiance)
    else:
        if False: #capture.dls_present():
            img_type='reflectance'
            capture.plot_undistorted_reflectance(capture.dls_irradiance())
        else:
            img_type = "radiance"
            capture.plot_undistorted_radiance()


    import cv2
    import numpy as np
    import matplotlib.pyplot as plt
    import micasense.imageutils as imageutils
    import micasense.plotutils as plotutils

    warp_mode = cv2.MOTION_HOMOGRAPHY
    #warp_matrices = capture.get_warp_matrices() # TODO : If we have a rig relative then we can use that to know the warp matricies

    warp_matrices, alignment_pairs = alignment(capture, "123")

    #cropped_dimensions,edges = imageutils.find_crop_bounds(capture,warp_matrices)
    cropped_dimensions, edges = imageutils.find_crop_bounds(capture, warp_matrices, warp_mode=warp_mode)

    im_aligned = imageutils.aligned_capture(capture, warp_matrices, warp_mode, cropped_dimensions, None, img_type=img_type)

    print("warp_matrices={}".format(warp_matrices))



    # figsize=(30,23) # use this size for full-image-resolution display
    figsize=(16,13)   # use this size for export-sized display

    rgb_band_indices = [2,1,0]
    cir_band_indices = [3,2,1]

    # Create an empty normalized stack for viewing
    im_display = np.zeros((im_aligned.shape[0],im_aligned.shape[1],capture.num_bands+1), dtype=np.float32 )

    im_min = np.percentile(im_aligned[:,:,0:2].flatten(),  0.1)  # modify with these percentilse to adjust contrast
    im_max = np.percentile(im_aligned[:,:,0:2].flatten(), 99.9)  # for many images, 0.5 and 99.5 are good values

    for i in range(0,im_aligned.shape[2]):
        if img_type == 'reflectance':
            # for reflectance images we maintain white-balance by applying the same display scaling to all bands
            im_display[:,:,i] =  imageutils.normalize(im_aligned[:,:,i], im_min, im_max)
        elif img_type == 'radiance':
            # for radiance images we do an auto white balance since we don't know the input light spectrum by
            # stretching each display band histogram to it's own min and max
            im_display[:,:,i] =  imageutils.normalize(im_aligned[:,:,i])

    rgb = im_display[:,:,rgb_band_indices]
    # for cir false color imagery, we normalize the NIR,R,G bands within themselves, which provides
    # the classical CIR rendering where plants are red and soil takes on a blue tint
    for i in cir_band_indices:
        im_display[:,:,i] =  imageutils.normalize(im_aligned[:,:,i])

    cir = im_display[:,:,cir_band_indices]
    fig, axes = plt.subplots(1, 2, figsize=figsize)
    axes[0].set_title("Red-Green-Blue Composite")
    axes[0].imshow(rgb)
    axes[1].set_title("Color Infrared (CIR) Composite")
    axes[1].imshow(cir)
    plt.show()


    # Create an enhanced version of the RGB render using an unsharp mask
    gaussian_rgb = cv2.GaussianBlur(rgb, (9,9), 10.0)
    gaussian_rgb[gaussian_rgb<0] = 0
    gaussian_rgb[gaussian_rgb>1] = 1
    unsharp_rgb = cv2.addWeighted(rgb, 1.5, gaussian_rgb, -0.5, 0)
    unsharp_rgb[unsharp_rgb<0] = 0
    unsharp_rgb[unsharp_rgb>1] = 1

    # Apply a gamma correction to make the render appear closer to what our eyes would see
    gamma = 1.4
    gamma_corr_rgb = unsharp_rgb**(1.0/gamma)
    fig = plt.figure(figsize=figsize)
    plt.imshow(gamma_corr_rgb, aspect='equal')
    plt.axis('off')
    plt.show()


    import imageio
    imtype = 'png' # or 'jpg'
    imageio.imwrite('rgb.'+imtype, (255*gamma_corr_rgb).astype('uint8'))



    from osgeo import gdal, gdal_array
    rows, cols, bands = im_display.shape
    driver = gdal.GetDriverByName('GTiff')
    filename = "bgrne" #blue,green,red,nir,redEdge

    if im_aligned.shape[2] == 6:
        filename = filename + "t" #thermal
    outRaster = driver.Create(filename+".tiff", cols, rows, im_aligned.shape[2], gdal.GDT_UInt16)

    normalize = (img_type == 'radiance') # normalize radiance images to fit with in UInt16

    # Output a 'stack' in the same band order as RedEdge/Alutm
    # Blue,Green,Red,NIR,RedEdge[,Thermal]
    # reflectance stacks are output with 32768=100% reflectance to provide some overhead for specular reflections
    # radiance stacks are output with 65535=100% radiance to provide some overhead for specular reflections

    # NOTE: NIR and RedEdge are not in wavelength order!

    multispec_min = np.min(im_aligned[:,:,1:5])
    multispec_max = np.max(im_aligned[:,:,1:5])

    for i in range(0,5):
        outband = outRaster.GetRasterBand(i+1)
        if normalize:
            outdata = imageutils.normalize(im_aligned[:,:,i],multispec_min,multispec_max)
        else:
            outdata = im_aligned[:,:,i]
            outdata[outdata<0] = 0
            outdata[outdata>2] = 2

        outdata = outdata*32767
        outdata[outdata<0] = 0
        outdata[outdata>65535] = 65535
        outband.WriteArray(outdata)
        outband.FlushCache()

    if im_aligned.shape[2] == 6:
        outband = outRaster.GetRasterBand(6)
        outdata = im_aligned[:,:,5] * 100 # scale to centi-C to fit into uint16
        outdata[outdata<0] = 0
        outdata[outdata>65535] = 65535
        outband.WriteArray(outdata)
        outband.FlushCache()
    outRaster = None

if __name__ == '__main__':
    main()
