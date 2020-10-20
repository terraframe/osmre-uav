package gov.osmre.uasdm.erossync;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3ToFTPCopier
{
  private final String ftpServer;
  
  private final AmazonS3 s3Client;
  
  private final String s3Bucket;
  
  public S3ToFTPCopier(String ftpServerUrl, String s3Bucket)
  {
    this.ftpServer = ftpServerUrl;
    this.s3Client = AmazonS3ClientBuilder.standard().build();
    this.s3Bucket = s3Bucket;
  }
  
  public void copyDirectory(final String s3SourcePath, String ftpTargetPath, final boolean recursive) throws IOException
  {
    final ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(this.s3Bucket).withMaxKeys(500);
    ListObjectsV2Result result;
    
    // Spaces are unfortunately not handled well by our FTP library.
    // Luckily IDM doesn't have spaces in S3 anyway.
    ftpTargetPath = ftpTargetPath.replace(" ", "");
    
    do {
      result = this.s3Client.listObjectsV2(this.s3Bucket, s3SourcePath);

      for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
        String key = objectSummary.getKey();
        
        if (!key.endsWith("/") && !key.contains("thumbnails/") && !key.contains("odm_all/"))
        {
          this.copyFile(key, ftpTargetPath + "/" + key.replace(" ", ""));
        }
      }
      req.setContinuationToken(result.getNextContinuationToken());
   } while(result.isTruncated() == true );
  }
  
  public void copyFile(final String s3SourcePath, final String ftpTargetPath) throws IOException
  {
    System.out.println("Copying file from S3 [" + this.s3Bucket + "/" + s3SourcePath + "] to FTP [" + ftpTargetPath + "]");
    
    try (S3Object s3 = downloadS3File(s3SourcePath))
    {
      try (InputStream is = s3.getObjectContent())
      {
        uploadFTPFile(ftpTargetPath, is);
      }
    }
  }
  
  private void uploadFTPFile(String uploadPath, InputStream is) throws IOException
  {
    StaticUserAuthenticator auth = new StaticUserAuthenticator(ftpServer, System.getenv("FTP_USERNAME"), System.getenv("FTP_PASSWORD"));
    FileSystemOptions opts = new FileSystemOptions();
    DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);

    FileSystemManager fsManager = VFS.getManager();
    try (FileObject fo = fsManager.resolveFile("ftp://" + ftpServer + "/" + uploadPath, opts))
    {
      IOUtils.copy(is, fo.getContent().getOutputStream());
    }
  }
  
  private S3Object downloadS3File(String key) throws IOException
  {
    return s3Client.getObject(new GetObjectRequest(this.s3Bucket, key));
  }
}
