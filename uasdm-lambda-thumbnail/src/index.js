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
var async = require('async');
var AWS = require('aws-sdk');
var gm = require('gm')
            .subClass({ imageMagick: true }); // Enable ImageMagick integration.
var util = require('util');

// constants
var MAX_WIDTH  = 300;
var MAX_HEIGHT = 150;

// get reference to S3 client 
var s3 = new AWS.S3();
 
exports.handler = function(event, context, callback) {
    // Read options from the event.
    console.log("Reading options from event:\n", util.inspect(event, {depth: 5}));
    var srcBucket = event.Records[0].s3.bucket.name;
    // Object key may have spaces or unicode non-ASCII characters.
    var srcKey    =
    decodeURIComponent(event.Records[0].s3.object.key.replace(/\+/g, " "));  
    var dstBucket = srcBucket;
    
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
    var imageType = typeMatch[1].toLowerCase();
    var supportedFormats = ['jpeg', 'jpg', 'png', 'gif', 'bmp', 'tif', 'tiff']; // Supported by gm, but not web browsers : 'fits', 'gray', 'graya', 'jng', 'mono', 'ico', 'jbig', 'tga'
    if (supportedFormats.indexOf(imageType) == -1) {
        callback('Unsupported image type: ' + imageType);
        return;
    }

    // Download the image from S3, transform, and upload to a different S3 bucket.
    async.waterfall([
        function download(next) {
            // Download the image from S3 into a buffer.
            s3.getObject({
                    Bucket: srcBucket,
                    Key: srcKey
                },
                next);
            },
        function transform(response, next) {
            gm(response.Body).size(function(err, size) {
            	if (err) {
                    callback(
                        'Unable to determine size of ' + srcBucket + '/' + srcKey +
                        ' due to an error: ' + err
                    );
                }
            	
                // Infer the scaling factor to avoid stretching the image unnaturally.
                var scalingFactor = Math.min(
                    MAX_WIDTH / size.width,
                    MAX_HEIGHT / size.height
                );
                var width  = scalingFactor * size.width;
                var height = scalingFactor * size.height;

                // Transform the image buffer in memory.
                this.resize(width, height)
                    .toBuffer('PNG', function(err, buffer) {
                        if (err) {
                            next(err);
                        } else {
                            next(null, response.ContentType, buffer);
                        }
                    });
            });
        },
        function upload(contentType, data, next) {
            // Stream the transformed image to a different S3 bucket.
            s3.putObject({
                    Bucket: dstBucket,
                    Key: dstKey,
                    Body: data,
                    ContentType: "image/png"
                },
                next);
        }
        ], function (err) {
            if (err) {
                console.error(
                    'Unable to resize ' + srcBucket + '/' + srcKey +
                    ' and upload to ' + dstBucket + '/' + dstKey +
                    ' due to an error: ' + err
                );
            } else {
                console.log(
                    'Successfully resized ' + srcBucket + '/' + srcKey +
                    ' and uploaded to ' + dstBucket + '/' + dstKey
                );
            }

            callback(null, "message");
        }
    );
};
