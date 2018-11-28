package gov.geoplatform.uasdm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ParameterValue;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.request.RequestDecorator;
import com.runwaysdk.request.ServletRequestIF;

import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.view.MultipartUploadParser;
import gov.geoplatform.uasdm.view.RequestParser;

@Controller(url = "file")
public class FileUploadController
{
  private class MergePartsException extends Exception
  {
    /**
     * 
     */
    private static final long serialVersionUID = 4441513458265403462L;

    MergePartsException(String message)
    {
      super(message);
    }
  }

  private static class PartitionFilesFilter implements FilenameFilter
  {
    private String filename;

    PartitionFilesFilter(String filename)
    {
      this.filename = filename;
    }

    @Override
    public boolean accept(File file, String s)
    {
      return s.matches(Pattern.quote(filename) + "_\\d+");
    }
  }

  final Logger                     log = LoggerFactory.getLogger(FileUploadController.class);

  private ProjectManagementService service;

  public FileUploadController()
  {
    this.service = new ProjectManagementService();
  }

  // @Endpoint(url = "upload", method = ServletMethod.POST, error =
  // ErrorSerialization.JSON, factory = ChunkFileItemFactory.class)
  @Endpoint(url = "upload", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF upload(ClientRequestIF clientRequest, ServletRequestIF request, @RequestParamter(name = "values") Map<String, ParameterValue> values)
  {
    HttpServletRequest req = ( (RequestDecorator) request ).getRequest();
    ServletContext context = req.getServletContext();

    boolean isIframe = req.getHeader("X-Requested-With") == null || !req.getHeader("X-Requested-With").equals("XMLHttpRequest");

    try
    {
      MultipartUploadParser multipartUploadParser = new MultipartUploadParser(values, AppProperties.getTempDirectory(), context);
      RequestParser requestParser = RequestParser.getInstance(req, multipartUploadParser);

      if (requestParser.isFirst() || requestParser.isResume())
      {
        // Validate that the collection name
        this.service.validate(clientRequest.getSessionId(), requestParser);
      }

      if (requestParser.isResume())
      {
        // Validate that the chunks still exist on the file system
        this.assertChunksExist(requestParser.getUuid());
      }

      this.writeFileForMultipartRequest(clientRequest, requestParser);

      return writeResponse(requestParser.generateError() ? "Generated error" : null, isIframe, false, false);
    }
    catch (Exception e)
    {
      log.error("Problem handling upload request", e);

      if (e instanceof MergePartsException)
      {
        return this.writeResponse(e.getMessage(), isIframe, true, false);
      }
      else if (e instanceof SmartExceptionDTO)
      {
        return this.writeResponse(e.getMessage(), isIframe, false, true);
      }

      return this.writeResponse(e.getMessage(), isIframe, false, false);
    }
  }

  private void writeFileForMultipartRequest(ClientRequestIF clientRequest, RequestParser requestParser) throws Exception
  {
    File dir = new File(AppProperties.getUploadDirectory(), requestParser.getUuid());
    dir.mkdirs();

    if (requestParser.getPartIndex() >= 0)
    {
      writeFile(requestParser.getUploadItem().getInputStream(), new File(dir, requestParser.getUuid() + "_" + String.format("%05d", requestParser.getPartIndex())), null);

      if (requestParser.getTotalParts() - 1 == requestParser.getPartIndex())
      {
        File[] parts = getPartitionFiles(dir, requestParser.getUuid());
        File outputFile = new File(dir, requestParser.getOriginalFilename());

        for (File part : parts)
        {
          this.mergeFiles(outputFile, part);
        }

        this.assertCombinedFileIsVaid(requestParser.getTotalFileSize(), outputFile, requestParser.getUuid());
        deletePartitionFiles(dir, requestParser.getUuid());

        this.service.handleUploadFinish(clientRequest.getSessionId(), requestParser, outputFile);
      }
    }
    else
    {
      writeFile(requestParser.getUploadItem().getInputStream(), new File(dir, requestParser.getFilename()), null);
    }
  }

  private void assertCombinedFileIsVaid(long totalFileSize, File outputFile, String uuid) throws MergePartsException
  {
    if (totalFileSize != outputFile.length())
    {
      deletePartitionFiles(AppProperties.getUploadDirectory(), uuid);

      outputFile.delete();

      throw new MergePartsException("Incorrect combined file size!");
    }

  }

  private void assertChunksExist(String uuid) throws MergePartsException
  {
    File dir = new File(AppProperties.getUploadDirectory(), uuid);

    if (dir.exists() && dir.isDirectory() && dir.list().length == 0)
    {
      throw new MergePartsException("Chunks no longer exist on the server file system.  Upload needs to be reset.");
    }
  }

  private File mergeFiles(File outputFile, File partFile) throws IOException
  {
    FileOutputStream fos = new FileOutputStream(outputFile, true);

    try
    {
      FileInputStream fis = new FileInputStream(partFile);

      try
      {
        IOUtils.copy(fis, fos);
      }
      finally
      {
        IOUtils.closeQuietly(fis);
      }
    }
    finally
    {
      IOUtils.closeQuietly(fos);
    }

    return outputFile;
  }

  private File writeFile(InputStream in, File out, Long expectedFileSize) throws IOException
  {
    FileOutputStream fos = null;

    try
    {
      fos = new FileOutputStream(out);

      IOUtils.copy(in, fos);

      if (expectedFileSize != null)
      {
        Long bytesWrittenToDisk = out.length();

        if (!expectedFileSize.equals(bytesWrittenToDisk))
        {
          log.warn("Expected file {} to be {} bytes; file on disk is {} bytes", new Object[] { out.getAbsolutePath(), expectedFileSize, 1 });

          out.delete();
          throw new IOException(String.format("Unexpected file size mismatch. Actual bytes %s. Expected bytes %s.", bytesWrittenToDisk, expectedFileSize));
        }
      }

      return out;
    }
    catch (

    Exception e)
    {
      throw new IOException(e);
    }
    finally
    {
      IOUtils.closeQuietly(fos);
    }
  }

  private ResponseIF writeResponse(String failureReason, boolean isIframe, boolean restartChunking, boolean preventRetry)
  {
    if (failureReason == null)
    {
      JSONObject response = new JSONObject();
      response.put("success", true);

      return new RestBodyResponse(response);
    }
    else
    {
      JSONObject response = new JSONObject();
      response.put("error", failureReason);

      if (restartChunking)
      {
        response.put("reset", true);
      }

      if (preventRetry)
      {
        response.put("preventRetry", true);
      }

      return new RestBodyResponse(response);
    }
  }

  private static File[] getPartitionFiles(File directory, String filename)
  {
    File[] files = directory.listFiles(new PartitionFilesFilter(filename));
    Arrays.sort(files);
    return files;
  }

  private static void deletePartitionFiles(File directory, String filename)
  {
    File[] partFiles = getPartitionFiles(directory, filename);

    for (File partFile : partFiles)
    {
      partFile.delete();
    }
  }
}
