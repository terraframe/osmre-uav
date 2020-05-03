package gov.geoplatform.uasdm.model;

import java.util.Date;
import java.util.List;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.dataaccess.MdClassDAOIF;

public interface ProductIF extends ComponentIF
{
  public MdClassDAOIF getMdClass();

  public String getOid();

  public String getName();

  public String getImageKey();

  public String getMapKey();

  public Date getLastUpdateDate();

  public String getBoundingBox();

  public void updateBoundingBox();

  public void clear();

  public void addDocuments(List<DocumentIF> documents);

  public UasComponentIF getComponent();

  public List<DocumentIF> getGeneratedFromDocuments();

  public Page<DocumentIF> getGeneratedFromDocuments(Integer pageNumber, Integer pageSize);

  public void calculateKeys(List<UasComponentIF> components);

  public void delete();

  public void createImageService();

  public String getWorkspace();

  public boolean isPublished();

  public void togglePublished();

}
