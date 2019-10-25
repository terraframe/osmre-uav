import os

from opendm import io
from opendm import log
from opendm import system
from opendm import context
from opendm import types
import shlex
import glob
import shutil
import subprocess


class ODMMicasenseStage(types.ODM_Stage):
    def process(self, args, outputs):
        log.ODM_INFO("args.multispectral = " + str(args.multispectral))

        if (args.multispectral):
            import subprocess

            tree = outputs['tree']

            micasense = io.join_paths(tree.root_path, 'micasense')
            micain = io.join_paths(micasense, 'in')
            micaout = io.join_paths(micasense, 'out')
            micathumb = io.join_paths(micaout, 'thumbnails')
            odmImages = tree.input_images

            if not io.dir_exists(micain):
                log.ODM_INFO("Micasense in directory %s doesn't exist. Creating it now. " % micain)
                system.mkdir_p(micain)

            log.ODM_INFO("Copying all files in " + odmImages)
            # Move all images from the odm images directory to our micasense/in directory
            for filePath in glob.glob(odmImages + '/*.*'):
                log.ODM_INFO("Moving file " + filePath + " to " + micain + ".")
                shutil.move(filePath, micain);

            # Wait for the OS to actually move the files...
            import time
            time.sleep(1)

            cmd = 'docker run --rm --mount type=bind,src=' + micasense + ',dst=/opt/micawork -e MICASENSE_OUT=/opt/micawork/out -e MICASENSE_IN=/opt/micawork/in micasense-docker'
            log.ODM_INFO("Running command " + cmd)

            #subprocess.check_call(cmd, shell=True, stderr=subprocess.STDOUT, stdout=subprocess.STDOUT)
            #self.run_command(cmd)

            subprocess.check_call(cmd, shell=True)

            # Move all micasense output images from micasense/out to the odmImages directory
            for filePath in glob.glob(micathumb + '/*.jpg'):
                log.ODM_INFO("Copying file " + filePath + " to " + odmImages + ".")
                shutil.copy(filePath, odmImages);
                shutil.copy(filePath, micasense);

            log.ODM_INFO("Removing directory " + micain)
            shutil.rmtree(micain)
            log.ODM_INFO("Removing directory " + micaout)
            shutil.rmtree(micaout)

    def run_command(self, command):
        log.ODM_INFO("Running command: " + str(command))

        process = subprocess.Popen(shlex.split(command), shell=True, stdout=subprocess.PIPE)
        while True:
            output = process.stdout.readline()
            if output == '' and process.poll() is not None:
                break
            if output:
                log.ODM_INFO("Micasense output: %s" % output.strip())
        rc = process.poll()
        return rc
