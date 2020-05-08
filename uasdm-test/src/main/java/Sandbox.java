/**
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
import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.bus.Imagery;
import gov.geoplatform.uasdm.bus.UasComponent;

import com.runwaysdk.session.Request;

public class Sandbox
{
  public static void main(String[] args) 
  {
//    GeoprismPatcher.main(args);
    
    System.out.println("Happy Testing!");
    
    UasComponent uasComponent = null;
    
    System.out.println(uasComponent instanceof Imagery);
    
//    test();
    
//    createFolder();
    
//    deleteFolder();
    
    
//    AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
//    
//    List<Bucket> bucketList = client.listBuckets();
//    
//    for (Bucket bucket : bucketList)
//    { 
//      if (bucket.getName().equals("BUCKET_NAME"))
//      {
//        System.out.println("Bucket Name: " +bucket.getName());
//      }
//    }   

    
//    GetObjectRequest request = new GetObjectRequest("BUCKET_NAME", "CottonwoodData/");        
//    S3Object object = client.getObject(request); 
//    System.out.println("Bucket Name: "+object.getBucketName());
//    System.out.println("Key: "+object.getKey());
    

//  ListObjectsRequest request = new ListObjectsRequest();
//  request = request.withBucketName("BUCKET_NAME");
//  request = request.withPrefix("CottonwoodData");
//    
//    ObjectListing listing;
//    
//    do
//    {
//      listing = client.listObjects(request);
//
//      List<S3ObjectSummary> summaries = listing.getObjectSummaries();
//
//      for (S3ObjectSummary summary : summaries)
//      {
//        String key = summary.getKey();
//
//        System.out.println("Key: "+key);
//        
////        if (key.endsWith(".xml.gz"))
////        {
////          files.add(key);
////        }
//      }
//
//      request.setMarker(listing.getNextMarker());
//    } while (listing != null && listing.isTruncated());
    
  }
//  
//  public static void createFolder()
//  {
//    AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
//    
//    // create meta-data for your folder and set content-length to 0
//    ObjectMetadata metadata = new ObjectMetadata();
//    metadata.setContentLength(0);
//
//    // create empty content
//    InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
//    
//    PutObjectRequest putObjectRequest = new PutObjectRequest("BUCKET_NAME",
//        "CottonwoodData/Project1/", emptyContent, metadata);
//    
//    // send request to S3 to create folder
//    client.putObject(putObjectRequest);
//  }
//  
//  public static void deleteFolder()
//  {
//    AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
//    
//    DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest("BUCKET_NAME")
//    .withKeys("CottonwoodData/Project2/")
//    .withQuiet(false);
//    
//    DeleteObjectsResult delObjRes = client.deleteObjects(multiObjectDeleteRequest);
//    int successfulDeletes = delObjRes.getDeletedObjects().size();
//    System.out.println(successfulDeletes + " objects successfully deleted.");
//  }
  
  @Request
  private static void test()
  {
    Bureau bureau = Bureau.getByKey("OSMRE");
    
    bureau.printAttributes();
  }
}


