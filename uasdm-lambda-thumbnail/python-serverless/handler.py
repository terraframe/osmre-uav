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


import boto3
from io import BytesIO
import re
from urllib.parse import unquote_plus
import pyvips
from smart_open import open

buffer_size = 10240

def resize_image(bucket, srcKey): 
	
	# Calculate our destination
	dstKey = "";
	if (srcKey.find("/") != -1):
		lastIndex = srcKey.rfind("/");
		lastPeriod = srcKey.rfind(".");
		dstKey = srcKey[0:lastIndex] + "/thumbnails" + srcKey[lastIndex:lastPeriod] + ".png";
	else:
		dstKey = "thumbnails/" + srcKey;
	
	# Don't process images inside a 'thumbnails' directory (infinite loop)
	if (srcKey.find("thumbnails/") != -1):
		return "Rejecting key [" + srcKey + "] because it is inside the thumbnails directory";
	
	# Skip the dem directory
	if (srcKey.find("dem/") != -1):
		return "Rejecting key [" + srcKey + "] because it is inside the dem directory";

	# Infer the image type.
	typeMatch = re.match(".*\.([^.]*)$", srcKey);
	if (not typeMatch):
		return "Could not determine the image type on source key: " + srcKey;
	
	# Check that the image type is supported
	imageType = typeMatch.groups()[0].lower();
	supportedFormats = ['jpeg', 'jpg', 'png', 'gif', 'bmp', 'tif', 'tiff'];
	try:
		supportedFormats.index(imageType)
	except ValueError:
			return 'Unsupported image type: ' + imageType;
	
	print("Will download image from " + srcKey + " and upload to " + dstKey)
	
	# Download the image from S3
	s3 = boto3.resource('s3')
	response = s3.Object(bucket_name=bucket, key=srcKey).get()
	streamingDownload = response['Body'] # is of type botocore.response.StreamingBody

	# Define a Vips Reader for the S3 StreamingBody
	def read_handler(size):
		return streamingDownload.read(amt=size)
	vipsSource = pyvips.SourceCustom()
	vipsSource.on_read(read_handler)
	
	# Open a connection to our s3 file
	with open("s3://" + bucket + "/" + dstKey, 'wb') as s3out:
	
		# Hook up our pyvips target to write to S3
		def write_handler(chunk):
			s3out.write(chunk)
		vipsTarget = pyvips.TargetCustom()
		vipsTarget.on_write(write_handler)
	
		# Kick off the pyvips streaming process
		image = pyvips.Image.new_from_source(vipsSource, '', access='sequential')
		image = image.thumbnail_image(300, height=300)
		image.write_to_target(vipsTarget, ".png")
	
	return "Successfully downloaded an image from " + bucket + "/" + srcKey + " and uploaded a thumbnail to " + bucket + "/" + dstKey;
	

# This method is a direct callback from lambda. The goal here is to convert the params
# From lambda into a generic format which can also be used locally for testing
def lambda_handler(event, context):
	msg = "";
	
	for record in event['Records']:
	
			bucket = record['s3']['bucket']['name']
			srcKey = unquote_plus(record['s3']['object']['key'])
			
			msg = msg + resize_image(bucket, srcKey) + "\n";
	
	print("Returning with message: " + msg)
	return {'message': msg};

# This method allows us to run our function locally for testing purposes
if __name__ == "__main__":
	class TestContext(object):	# fake object
			function_name = "++MAIN++"
	
	test_event = {
		"Records": [
			{
				"s3": {
					"bucket": {
						"name": "terraframe-test-bucket"
					},
					"object": {
						"key": "uasdm-lambda-test/230m.png"
					}
				}
			}
		]
	}
	lambda_handler(test_event, TestContext())

				