package gov.geoplatform.uasdm.bus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.uasdm.AppProperties;

public class Collection extends CollectionBase
{
  private static final long  serialVersionUID = 1371809368;

  public static final int    BUFFER_SIZE      = 1024;

  public static final String RAW              = "raw";

  public static final String PTCLOUD          = "ptcloud";

  public static final String DEM              = "dem";

  public static final String ORTHO            = "ortho";

  public Collection()
  {
    super();
  }

  /**
   * Returns null, as a Collection cannot have a child.
   */
  public UasComponent createChild()
  {
    // TODO throw exception.

    return null;
  }

  public ComponentHasComponent addComponent(UasComponent uasComponent)
  {
    return this.addMission((Mission) uasComponent);
  }

  /**
   * Creates the object and builds the relationship with the parent.
   * 
   * Creates directory in S3.
   * 
   * @param parent
   */
  @Transaction
  @Override
  public void applyWithParent(UasComponent parent)
  {
    super.applyWithParent(parent);

    if (this.isNew())
    {
      this.createS3Folder(this.buildRawKey());

      this.createS3Folder(this.buildPointCloudKey());

      this.createS3Folder(this.buildDemKey());

      this.createS3Folder(this.buildOrthoKey());
    }
  }

  public void delete()
  {
    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.buildRawKey());

      this.deleteS3Folder(this.buildPointCloudKey());

      this.deleteS3Folder(this.buildDemKey());

      this.deleteS3Folder(this.buildOrthoKey());
    }
  }

  private String buildRawKey()
  {
    return this.getS3location() + RAW + "/";
  }

  private String buildPointCloudKey()
  {
    return this.getS3location() + PTCLOUD + "/";
  }

  private String buildDemKey()
  {
    return this.getS3location() + DEM + "/";
  }

  private String buildOrthoKey()
  {
    return this.getS3location() + ORTHO + "/";
  }

  public void uploadArchive(File archive)
  {
    String extension = FilenameUtils.getExtension(archive.getName());

    if (extension.equalsIgnoreCase("zip"))
    {
      this.uploadZipArchive(archive);
    }
    else if (extension.equalsIgnoreCase("gz"))
    {
      this.uploadTarGzArchive(archive);
    }
  }

  private void uploadTarGzArchive(File archive)
  {
    byte data[] = new byte[BUFFER_SIZE];

    try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(new FileInputStream(archive)))
    {
      try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn))
      {
        TarArchiveEntry entry;

        while ( ( entry = (TarArchiveEntry) tarIn.getNextEntry() ) != null)
        {
          /** If the entry is a directory, create the directory. **/
          if (entry.isDirectory())
          {
            File f = new File(entry.getName());
            boolean created = f.mkdir();
            if (!created)
            {
              System.out.printf("Unable to create directory '%s', during extraction of archive contents.\n", f.getAbsolutePath());
            }
          }
          else
          {
            File tmp = File.createTempFile("raw", "tmp");

            try
            {
              try (FileOutputStream fos = new FileOutputStream(tmp))
              {
                int count;

                try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE))
                {
                  while ( ( count = tarIn.read(data, 0, BUFFER_SIZE) ) != -1)
                  {
                    dest.write(data, 0, count);
                  }
                }
              }

              // Upload the file to S3
              this.uploadRawFile(entry.getName(), tmp);
            }
            finally
            {
              FileUtils.deleteQuietly(tmp);
            }

          }
        }

        System.out.println("Untar completed successfully!");
      }
    }
    catch (IOException | AmazonClientException | InterruptedException e)
    {
      // TODO give better error message?
      throw new ProgrammingErrorException(e);
    }
  }

  public void uploadZipArchive(File archive)
  {
    byte[] buffer = new byte[BUFFER_SIZE];

    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(archive)))
    {
      ZipEntry entry;
      while ( ( entry = zis.getNextEntry() ) != null)
      {
        File tmp = File.createTempFile("raw", "tmp");

        try
        {
          try (FileOutputStream fos = new FileOutputStream(tmp))
          {
            int len;
            while ( ( len = zis.read(buffer) ) > 0)
            {
              fos.write(buffer, 0, len);
            }
          }

          // Upload the file to S3
          this.uploadRawFile(entry.getName(), tmp);
        }
        finally
        {
          FileUtils.deleteQuietly(tmp);
        }
      }
    }
    catch (IOException | AmazonClientException | InterruptedException e)
    {
      // TODO give better error message?
      throw new ProgrammingErrorException(e);
    }
  }

  private void uploadRawFile(String name, File tmp) throws IOException, AmazonClientException, InterruptedException
  {
    if (isValidName(name))
    {
      String key = this.buildRawKey() + name;

      TransferManager tx = new TransferManager(new ClasspathPropertiesFileCredentialsProvider());
      Upload myUpload = tx.upload(AppProperties.getBucketName(), key, tmp);

      // You can poll your transfer's status to check its progress
      if (myUpload.isDone() == false)
      {
        System.out.println("Transfer: " + myUpload.getDescription());
        System.out.println("  - State: " + myUpload.getState());
        System.out.println("  - Progress: " + myUpload.getProgress().getBytesTransferred());
      }

      // Transfers also allow you to set a <code>ProgressListener</code> to
      // receive
      // asynchronous notifications about your transfer's progress.
      myUpload.addProgressListener(new ProgressListener()
      {
        int count = 0;

        @Override
        public void progressChanged(ProgressEvent progressEvent)
        {
          if (count % 2000 == 0)
          {
            long total = myUpload.getProgress().getTotalBytesToTransfer();
            long current = myUpload.getProgress().getBytesTransferred();
            System.out.println(current + "/" + total + "-" + ( (int) ( (double) current / total * 100 ) ) + "%");

            count = 0;
          }

          count++;
        }
      });

      // Or you can block the current thread and wait for your transfer to
      // to complete. If the transfer fails, this method will throw an
      // AmazonClientException or AmazonServiceException detailing the reason.
      myUpload.waitForCompletion();

      // After the upload is complete, call shutdownNow to release the
      // resources.
      tx.shutdownNow();
    }
    else
    {
      System.out.println("Name [" + name + "] is invalid");
    }
  }
}
