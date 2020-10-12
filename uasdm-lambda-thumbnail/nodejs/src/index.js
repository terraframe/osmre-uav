/*
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// dependencies
const AWS = require('aws-sdk');
const util = require('util');
const sharp = require('sharp');
const downloader = require('s3-download-stream');
const uploader = require('s3-upload-stream');

// constants
const THUMBNAIL_WIDTH = 300; // set thumbnail width. Resize will set the height automatically to maintain aspect ratio.

// get reference to S3 client 
const s3 = new AWS.S3();
 
exports.handler = async (event, context, callback) => {

    // Read options from the event parameter.
    console.log("Reading options from event:\n", util.inspect(event, {depth: 5}));
    const srcBucket = event.Records[0].s3.bucket.name;
    // Object key may have spaces or unicode non-ASCII characters.
    const srcKey    = decodeURIComponent(event.Records[0].s3.object.key.replace(/\+/g, " "));
    const dstBucket = srcBucket;
    
    // Calculate our destination
    var dstKey = "";
    if (srcKey.indexOf("/") != -1)
    {
	  var lastIndex = srcKey.lastIndexOf("/");
	  var lastPeriod = srcKey.lastIndexOf(".");
	  dstKey = srcKey.slice(0, lastIndex) + "/thumbnails" + srcKey.slice(lastIndex, lastPeriod) + ".png";
    }
    else
    {
      dstKey = "thumbnails/" + srcKey;
    }

    // Don't process images inside a 'thumbnails' directory (infinite loop)
    if (srcKey.indexOf("thumbnails/") != -1)
    {
      callback("Rejecting key [" + srcKey + "] because it is inside the thumbnails directory");
      return;
    }
    // Skip the dem directory
    if (srcKey.indexOf("dem/") != -1)
    {
      callback("Rejecting key [" + srcKey + "] because it is inside the dem directory");
      return;
    }

    // Infer the image type.
    var typeMatch = srcKey.match(/\.([^.]*)$/);
    if (!typeMatch) {
        callback("Could not determine the image type.");
        return;
    }
    
    // Check that the image type is supported
    const imageType = typeMatch[1].toLowerCase();
    var supportedFormats = ['jpeg', 'jpg', 'png', 'gif', 'bmp', 'tif', 'tiff'];
    if (supportedFormats.indexOf(imageType) == -1) {
        callback('Unsupported image type: ' + imageType);
        return;
    }

    // Download the image from S3 as a stream
    var config = {
      client: s3,
      concurrency: 6,
      params: {
        Key: srcKey,
        Bucket: srcBucket
      }
    }
     
	// Use the Sharp module to resize the image and save in a buffer.
	const sharpResizer =
	  sharp({limitInputPixels: false, sequentialRead:true})
	    .resize(THUMBNAIL_WIDTH)
	    .png();
    
    var upload = uploader(s3).upload({
    	  "Bucket": dstBucket,
    	  "Key": dstKey
    	});
    upload.maxPartSize(20971520); // 20 MB
    upload.concurrentParts(5);
    
	const uploadWaitPromise = new Promise( (resolve, reject) => {
		upload.on('error', function (error) {
	      console.error(error);
	      resolve();
	    });
	    
	    upload.on('uploaded', function (details) {
	  	  console.log('Successfully resized ' + srcBucket + '/' + srcKey + ' and uploaded to ' + dstBucket + '/' + dstKey, details);
	  	  resolve();
	  	});
	  	 
	  	downloader(config)
	  	  .pipe(sharpResizer)
	  	  .pipe(upload);
	});
	
	await uploadWaitPromise;
};
