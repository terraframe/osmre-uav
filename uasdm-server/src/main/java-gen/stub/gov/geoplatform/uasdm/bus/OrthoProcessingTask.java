package gov.geoplatform.uasdm.bus;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.processing.GdalDemProcessor;
import gov.geoplatform.uasdm.processing.GdalTransformProcessor;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
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

    // Create the png for the uploaded file
    try (CloseableFile file = infile.openNewFile())
    {
      if (this.getUploadTarget().equals(ImageryComponent.ORTHO) && this.getProcessOrtho())
      {
        new GdalTransformProcessor(FilenameUtils.getBaseName(file.getName()) + ".png", this, product, collection, ImageryComponent.ORTHO).process(file, null);
      }

      if (this.getUploadTarget().equals(ImageryComponent.DEM) && this.getProcessDem())
      {
        new GdalDemProcessor("dsm.tif", this, product, collection, ODMZipPostProcessor.DEM_GDAL).process(file, null);
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
