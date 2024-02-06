package gov.geoplatform.uasdm.bus;

import java.util.List;

import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.UAV;
import gov.geoplatform.uasdm.model.CollectionIF;

public class ErrorReport extends ErrorReportBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1076869468;
  
  public ErrorReport()
  {
    super();
  }
  
  public void Populate(CollectionIF col, AbstractWorkflowTask task)
  {
    Sensor sensor = col.getSensor();
    if (sensor != null)
    {
      this.setSensorName(sensor.getName());
      this.setSensorType(sensor.getSensorType().getName());
    }
    
    this.setCollectionName(col.getName());
    this.setCollectionS3Path(col.getS3location());
    
    this.setCollectionSize((long) col.getDocuments().size());
    
    List<ODMRun> odmRuns = ODMRun.getByComponentOrdered(col.getOid());
    
    if (odmRuns.size() > 0)
    {
      ODMRun odmRun = odmRuns.get(odmRuns.size()-1);
      
      this.setOdmConfig(odmRun.getConfig());
    }
    
    this.setFailReason(task.getMessage());
    
    this.setErrorDate(task.getLastUpdateDate());
    
    this.setCollectionPocName(col.getPocName());
    
    this.setCollectionId(col.getOid());
    
    UAV uav = col.getUav();
    
    if (uav != null)
    {
      this.setUavFaaId(uav.getFaaNumber());
      this.setUavSerialNumber(uav.getSerialNumber());
    }
  }
  
}
