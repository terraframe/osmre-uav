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
//      if (bucket.getName().equals("osmre-uas-repo"))
//      {
//        System.out.println("Bucket Name: " +bucket.getName());
//      }
//    }   

    
//    GetObjectRequest request = new GetObjectRequest("osmre-uas-repo", "CottonwoodData/");        
//    S3Object object = client.getObject(request); 
//    System.out.println("Bucket Name: "+object.getBucketName());
//    System.out.println("Key: "+object.getKey());
    

//  ListObjectsRequest request = new ListObjectsRequest();
//  request = request.withBucketName("osmre-uas-repo");
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
  
  public static void createFolder()
  {
    AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
    
    // create meta-data for your folder and set content-length to 0
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(0);

    // create empty content
    InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
    
    PutObjectRequest putObjectRequest = new PutObjectRequest("osmre-uas-repo",
        "CottonwoodData/Project1/", emptyContent, metadata);
    
    // send request to S3 to create folder
    client.putObject(putObjectRequest);
  }
  
  public static void deleteFolder()
  {
    AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
    
    DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest("osmre-uas-repo")
    .withKeys("CottonwoodData/Project2/")
    .withQuiet(false);
    
    DeleteObjectsResult delObjRes = client.deleteObjects(multiObjectDeleteRequest);
    int successfulDeletes = delObjRes.getDeletedObjects().size();
    System.out.println(successfulDeletes + " objects successfully deleted.");
  }
}

/* 
https://docs.aws.amazon.com/AmazonS3/latest/dev/UsingMetadata.html 
 
Characters That Might Require Special Handling
The following characters in a key name might require additional code handling and likely will need to be URL encoded or referenced as HEX. Some of these are non-printable characters and your browser might not handle them, which also requires special handling:

Ampersand ("&")

Dollar ("$")

ASCII character ranges 00–1F hex (0–31 decimal) and 7F (127 decimal)

'At' symbol ("@")

Equals ("=")

Semicolon (";")

Colon (":")

Plus ("+")

Space – Significant sequences of spaces may be lost in some uses (especially multiple spaces)

Comma (",")

Question mark ("?")

Characters to Avoid
Avoid the following characters in a key name because of significant special handling for consistency across all applications.

Backslash ("\")

Left curly brace ("{")

Non-printable ASCII characters (128–255 decimal characters)

Caret ("^")

Right curly brace ("}")

Percent character ("%")

Grave accent / back tick ("`")

Right square bracket ("]")

Quotation marks

'Greater Than' symbol (">")

Left square bracket ("[")

Tilde ("~")

'Less Than' symbol ("<")

'Pound' character ("#")

Vertical bar / pipe ("|")
*/
