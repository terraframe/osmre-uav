package gov.geoplatform.uasdm.model;

import java.util.List;

import com.amazonaws.services.s3.model.S3Object;
import com.runwaysdk.ComponentIF;
import com.runwaysdk.dataaccess.MdClassDAOIF;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.system.Actor;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.SiteObject;

public interface UasComponentIF extends ComponentIF
{
  public void apply();

  public void delete();

  public void delete(String key);

  public String getOid();

  public String getName();

  public void setName(String name);

  public String getDescription();

  public String getSolrIdField();

  public String getSolrNameField();

  public String getStoreName(String s3location);

  public UasComponentIF createChild(String type);

  public List<AttributeType> attributes();

  public void setValue(String name, Object value);

  public void setGeoPoint(Point geometry);

  public MdClassDAOIF getMdClass();

  public Object getObjectValue(String name);

  public Geometry getGeoPoint();

  public Integer getNumberOfChildren();

  public void applyWithParent(UasComponentIF parent);

  public String getS3location();

  public List<SiteObject> getSiteObjects(String key);

  public List<DocumentIF> getDocuments();

  public AbstractWorkflowTask createWorkflowTask(String uuid);

  public Actor getOwner();

  public List<UasComponentIF> getAncestors();

  public S3Object download(String key);

  public List<String> uploadArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget);

  public List<String> uploadZipArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget);

  public DocumentIF createDocumentIfNotExist(String key, String name);

  public ProductIF createProductIfNotExist();

  public List<ProductIF> getDerivedProducts();

  public List<UasComponentIF> getChildren();

  public UasComponentIF getChild(String name);

  public UasComponentIF createDefaultChild();

  public static boolean isValidName(String name)
  {
    if (name.contains(" ") || name.contains("<") || name.contains(">") || name.contains("-") || name.contains("+") || name.contains("=") || name.contains("!") || name.contains("@") || name.contains("#") || name.contains("$") || name.contains("%") || name.contains("^") || name.contains("&") || name.contains("*") || name.contains("?") || name.contains(";") || name.contains(":") || name.contains(",") || name.contains("^") || name.contains("{") || name.contains("}") || name.contains("]") || name.contains("[") || name.contains("`") || name.contains("~") || name.contains("|") || name.contains("/") || name.contains("\\"))
    {
      return false;
    }

    return true;
  }

}
