package gov.geoplatform.uasdm.cog;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.AppProperties;
import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
import it.geosolutions.imageioimpl.plugins.cog.CogImageInputStreamSpi;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReader;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReaderSpi;
import it.geosolutions.imageioimpl.plugins.cog.S3RangeReader;

public class CogTiffS3PreviewReader
{
  public static final Logger logger = LoggerFactory.getLogger(CogTiffS3PreviewReader.class);
  
  /**
   * 
   * @param s3url S3 path to the resource of the format 's3://bucketName/key'
   * @param preferredImage A number representing the preferred image overlay to grab from the tif file. If the preferred image is not available, one of a higher number will be requested in its place. For images without overlays, the full image can be requested via index 0.
   * @return
   */
  public static InputStream read(String s3url, int preferredImage) throws ProgrammingErrorException
  {
    for (int i = preferredImage; i >= 0; --i)
    {
      try
      {
	    final String protocol = "iio.s3";
	    
	    System.setProperty(protocol + ".aws.user", AppProperties.getS3AccessKey());
	    System.setProperty(protocol + ".aws.password", AppProperties.getS3SecretKey());
	    System.setProperty(protocol + ".aws.region", AppProperties.getBucketRegion());
	    
	    BasicAuthURI cogUri = new BasicAuthURI(s3url, false);
	    ImageInputStream cogStream = new CogImageInputStreamSpi().createInputStreamInstance(cogUri);

	    CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
	    reader.setInput(cogStream);

	    CogImageReadParam param = new CogImageReadParam();
	    param.setRangeReaderClass(S3RangeReader.class);
	    
	    BufferedImage cogImage = reader.read(i, param);
        
        final PipedInputStream istream = new PipedInputStream();
        final PipedOutputStream ostream = new PipedOutputStream(istream);

        Thread t = new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            try
            {
              ImageIO.write(cogImage, "png", ostream);
            }
            catch (IOException e)
            {
              e.printStackTrace();
            }
          }
        });
        t.setDaemon(true);
        t.start();

        return istream;
      }
      catch (IOException ex)
      {
        throw new ProgrammingErrorException("Unable to fetch an image from url " + s3url, ex);
      }
      catch (IndexOutOfBoundsException ex)
      {
        // Might happen if the requested overview number doesn't exist in the file.
      }
    }
    
    throw new ProgrammingErrorException("Unable to fetch an image from url " + s3url);
  }
}
