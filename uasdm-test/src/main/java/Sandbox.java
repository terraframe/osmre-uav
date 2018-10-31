import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class Sandbox
{
  public static void main(String[] args) 
  {
//    GeoprismPatcher.main(args);
    
    System.out.println("Happy Testing!");
    
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
}


