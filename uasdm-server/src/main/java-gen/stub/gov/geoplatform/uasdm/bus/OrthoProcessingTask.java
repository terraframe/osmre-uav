package gov.geoplatform.uasdm.bus;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.processing.CogTifProcessor;
import gov.geoplatform.uasdm.processing.CogTifValidator;
import gov.geoplatform.uasdm.processing.GdalTransformProcessor;
import gov.geoplatform.uasdm.processing.HillshadeProcessor;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
import gov.geoplatform.uasdm.processing.S3FileUpload;
import gov.geoplatform.uasdm.processing.StatusMonitorIF;
import gov.geoplatform.uasdm.processing.WorkflowTaskMonitor;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;

public class OrthoProcessingTask extends OrthoProcessingTaskBase
{
  private static final long serialVersionUID = -90821820;

  private static Logger logger = LoggerFactory.getLogger(OrthoProcessingTask.class);

  public OrthoProcessingTask()
  {
    super();
  }

  public String getImageryComponentOid()
  {
    return this.getComponent();
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject obj = super.toJSON();

    return obj;
  }

  public void initiate(ApplicationResource infile)
  {
    Collection collection = Collection.get(this.getComponent());

    Product product = (Product) collection.createProductIfNotExist();

    List<DocumentIF> documents = collection.getDocuments().stream().filter(doc -> {
      return doc.getS3location().contains("/" + this.getUploadTarget() + "/");
    }).collect(Collectors.toList());

    product.addDocuments(documents);

    try (CloseableFile file = infile.openNewFile())
    {
      final String basename = FilenameUtils.getBaseName(file.getName());
      final StatusMonitorIF monitor = new WorkflowTaskMonitor(this);
      
      if (this.getUploadTarget().equals(ImageryComponent.ORTHO) && this.getProcessOrtho())
      {
        if (!new CogTifValidator().isValidCog(file))
        {
          if (!new CogTifProcessor(ImageryComponent.ORTHO + "/" + basename + CogTifProcessor.COG_EXTENSION, product, collection, monitor).process(file))
          {
            // Fallback! If the cog tif processing fails, we're going to just upload the regular tiff to the cog tif location. That way they can at least view a basic ortho.
            // This usecase can happen if for example the cog validator is not installed.
            new S3FileUpload(ImageryComponent.ORTHO + "/" + basename + CogTifProcessor.COG_EXTENSION, collection, monitor, false).process(file);
          }
        }
        
        new GdalTransformProcessor(ImageryComponent.ORTHO + "/" + basename + ".png", product, collection, monitor).process(file);
      }

      if (this.getUploadTarget().equals(ImageryComponent.DEM) && this.getProcessDem())
      {
        new HillshadeProcessor(ODMZipPostProcessor.DEM_GDAL + "/dsm.tif", product, collection, new WorkflowTaskMonitor(this)).process(file);
      }

      if (product.getPublished())
      {
        for (DocumentIF mappable : product.getMappableDocuments())
        {
          RemoteFileFacade.copyObject(mappable.getS3location(), AppProperties.getBucketName(), mappable.getS3location(), AppProperties.getPublicBucketName());
        }
      }

      product.updateBoundingBox();

      this.appLock();
      this.setStatus(ODMStatus.COMPLETED.getLabel());
      this.setMessage("Ortho has been processed");
      this.apply();
    }
  }
}
