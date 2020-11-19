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
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;

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
    StaticUserAuthenticator auth = new StaticUserAuthenticator(ftpServer, System.getenv("EROSSYNC_FTP_USERNAME"), System.getenv("EROSSYNC_FTP_PASSWORD"));
    FileSystemOptions opts = new FileSystemOptions();
    DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
    
    if (System.getenv("EROSSYNC_FTP_PASSIVE") != null && (System.getenv("EROSSYNC_FTP_PASSIVE").equalsIgnoreCase("true") || System.getenv("EROSSYNC_FTP_PASSIVE").equalsIgnoreCase("yes")))
    {
      FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
    }

    FileSystemManager fsManager = VFS.getManager();
    try (FileObject fo = fsManager.resolveFile(this.buildFtpUrl() + uploadPath, opts))
    {
      IOUtils.copy(is, fo.getContent().getOutputStream());
    }
  }
  
  private String buildFtpUrl()
  {
    String url = "ftp://" + ftpServer;
    
    if (System.getenv("EROSSYNC_FTP_PORT") != null && System.getenv("EROSSYNC_FTP_PORT").length() > 0)
    {
      url = url + ":" + System.getenv("EROSSYNC_FTP_PORT");
    }
    
    return url + "/";
  }
  
  private S3Object downloadS3File(String key) throws IOException
  {
    return s3Client.getObject(new GetObjectRequest(this.s3Bucket, key));
  }
}
