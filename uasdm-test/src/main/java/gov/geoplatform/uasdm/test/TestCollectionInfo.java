package gov.geoplatform.uasdm.test;

import java.util.Date;

import gov.geoplatform.uasdm.bus.AllPrivilegeType;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.UasComponent;

public class TestCollectionInfo extends TestUasComponentInfo
{
  private Date collectionDate;

  private TestSensorInfo sensor;

  private Integer imageHeight;

  private Integer imageWidth;

  private Boolean metadataUploaded;

  private String pocEmail;

  private String pocName;

  private AllPrivilegeType privilegeType;

  private TestUavInfo uav;

  private TestMissionInfo mission;

  public TestCollectionInfo(String name, Date collectionDate, TestUavInfo uav, TestSensorInfo sensor, TestMissionInfo mission)
  {
    super(name, name, name, null);
    this.collectionDate = collectionDate;
    this.uav = uav;
    this.sensor = sensor;
    this.mission = mission;
  }

  public TestMissionInfo getMission()
  {
    return mission;
  }

  /**
   * Populates the component with the values contained within this wrapper
   */
  @Override
  public void populate(UasComponent component)
  {
    super.populate(component);

    Collection col = (Collection) component;

    col.setCollectionDate(this.getCollectionDate());
    col.setSensor(this.getSensor().getServerObject());
    col.setImageHeight(this.getImageHeight());
    col.setImageWidth(this.getImageWidth());
    col.setMetadataUploaded(this.getMetadataUploaded());
    col.setPocEmail(this.getPocEmail());
    col.setPocName(this.getPocName());
    col.setUav(this.getUav().getServerObject());

    col.clearPrivilegeType();
    col.addPrivilegeType(this.getPrivilegeType());
  }

  @Override
  public void apply()
  {
    super.apply(this.mission);
  }
  
  @Override
  public Collection getServerObject()
  {
    return (Collection) super.getServerObject();
  }

  /**
   * Creates a new instance of the server object type.
   */
  @Override
  public UasComponent instantiate()
  {
    return new Collection();
  }

  public Date getCollectionDate()
  {
    return collectionDate;
  }

  public void setCollectionDate(Date collectionDate)
  {
    this.collectionDate = collectionDate;
  }

  public TestSensorInfo getSensor()
  {
    return sensor;
  }

  public void setSensor(TestSensorInfo sensor)
  {
    this.sensor = sensor;
  }

  public Integer getImageHeight()
  {
    return imageHeight;
  }

  public void setImageHeight(Integer imageHeight)
  {
    this.imageHeight = imageHeight;
  }

  public Integer getImageWidth()
  {
    return imageWidth;
  }

  public void setImageWidth(Integer imageWidth)
  {
    this.imageWidth = imageWidth;
  }

  public Boolean getMetadataUploaded()
  {
    return metadataUploaded;
  }

  public void setMetadataUploaded(Boolean metadataUploaded)
  {
    this.metadataUploaded = metadataUploaded;
  }

  public String getPocEmail()
  {
    return pocEmail;
  }

  public void setPocEmail(String pocEmail)
  {
    this.pocEmail = pocEmail;
  }

  public String getPocName()
  {
    return pocName;
  }

  public void setPocName(String pocName)
  {
    this.pocName = pocName;
  }

  public AllPrivilegeType getPrivilegeType()
  {
    return privilegeType;
  }

  public void setPrivilegeType(AllPrivilegeType privilegeType)
  {
    this.privilegeType = privilegeType;
  }

  public TestUavInfo getUav()
  {
    return uav;
  }

  public void setUav(TestUavInfo uav)
  {
    this.uav = uav;
  }
}
