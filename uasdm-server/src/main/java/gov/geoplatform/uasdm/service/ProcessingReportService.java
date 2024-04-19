package gov.geoplatform.uasdm.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.Site;
import gov.geoplatform.uasdm.graph.UAV;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;

@Service
public class ProcessingReportService
{
  public static class ErrorReportRecord
  {
    public String collectionName;
    public String processedImageCount;
    public String processedImageSizeMb;
    public String taskMessage;
    public String processingStatus;
    public String runDate;
    public String pocName;
    public String organizationName;
    public String s3Path;
    public String uavSerial;
    public String uavFAA;
    public String sensorName;
    public String sensorType;
    public String featureQuality;
    public String radiometricCalibration;
    public String odmConfig;
  }
  
  public String[] header = new String[] {
    "collectionName", "processedImageCount", "processedImageSizeMb", "taskMessage", "processingStatus", "runDate",
    "pocName", "organizationName", "s3Path", "uavSerial", "uavFAA", "sensorName", "sensorType", "featureQuality",
    "radiometricCalibration", "odmConfig"
  };
  
  @Request(RequestType.SESSION)
  public InputStream generate(String sessionId, Date since)
  {
    StringWriter sw = new StringWriter();
    try (CSVWriter csv = new CSVWriter(sw))
    {
      csv.writeNext(header);
      
      for (ErrorReportRecord record : getRecords(since))
      {
        String[] line = new String[header.length];
        
        for (int i = 0; i < header.length; ++i)
        {
          try
          {
            line[i] = (String) record.getClass().getDeclaredField(header[i]).get(record);
          }
          catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
          {
            throw new ProgrammingErrorException(e);
          }
        }
        
        csv.writeNext(line);
      }
      
      csv.flush();
      
      return new ByteArrayInputStream(sw.toString().getBytes());
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  /*
  private void buildRecord(Map<String, Object> values)
  {
    Collection col = null;
    Sensor sensor = null;
    UAV uav = null;
    try
    {
      col = (Collection) run.getComponent();
      sensor = col.getSensor();
      uav = col.getUav();
    }
    catch (Exception e) {}
    
    WorkflowTask task = null;
    try
    {
      task = run.getWorkflowTask();
    }
    catch (Exception e) {}
    
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    ErrorReportRecord record = new ErrorReportRecord();
    
    if (col != null)
    {
      record.collectionName = col.getName();
      record.pocName = col.getPocName();
      record.s3Path = col.getS3location();
      
      List<UasComponentIF> ancestors = col.getAncestors();
      record.organizationName = ((Site) ancestors.get(ancestors.size() - 1)).getServerOrganization().getDisplayLabel().getValue();
    }
    
    List<Document> images = run.getODMRunInputParentDocuments();
    record.processedImageCount = String.valueOf(images.size());
    record.processedImageSizeMb = String.valueOf(images.stream().map(i -> i.getFileSize()).reduce(0L, (a,b) -> a + b));
        
    if (task != null)
    {
      record.taskMessage = task.getMessage();
      record.processingStatus = task.getNormalizedStatus();
      record.errorDate = dateFormat.format(task.getLastUpdateDate());
    }
    
    if (uav != null)
    {
      record.uavSerial = uav.getSerialNumber();
      record.uavFAA = uav.getFaaNumber();
    }
    
    if (sensor != null)
    {
      record.sensorName = sensor.getName();
      record.sensorType = sensor.getType();
    }
    
    record.odmConfig = run.getConfig();
    ODMProcessConfiguration config = run.getConfiguration();
    record.featureQuality = config.getFeatureQuality().name();
    record.radiometricCalibration = config.getRadiometricCalibration().name();
    
    return record;
  }
  */
  
  private static List<ErrorReportRecord> getRecords(Date since)
  {
    final String clazz = MdVertexDAO.getMdVertexDAO(ODMRun.CLASS).getDBClassName();
    final Map<String, Object> params = new HashMap<String, Object>();

    // the odm output is gigantic so we have to make sure we don't select it
    StringBuilder builder = new StringBuilder();
    builder.append("SELECT"
        + " oid,config,runStart,workflowTask,"
        + " in().fileSize,"
        + " component.oid,component.name,component.s3location,component.pocName,component.collectionSensor.oid,component.uav.oid FROM " + clazz);
    
    if (since != null)
    {
      builder.append(" WHERE " + ODMRun.RUNSTART + " >= :since");
      params.put("since", since);
    }
    
    builder.append(" ORDER BY " + ODMRun.RUNSTART + " desc");
    
    builder.append(" LIMIT 10000");
    
    final GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(builder.toString(), params);
    
    List<ErrorReportRecord> results = new ArrayList<ErrorReportRecord>();
    
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    query.getRawResults().forEach(map -> {
      ErrorReportRecord result = new ErrorReportRecord();
      result.collectionName = (String) map.get("component.name");
      result.s3Path = (String) map.get("component.s3location");
      result.pocName = (String) map.get("component.pocName");
      result.odmConfig = (String) map.get("config");
      result.runDate = dateFormat.format((Date) map.get("runStart"));
      
      @SuppressWarnings("unchecked")
      List<Long> fileSizes = (List<Long>) map.get("in().fileSize");
      result.processedImageCount = String.valueOf(fileSizes.size());
      result.processedImageSizeMb = String.valueOf(fileSizes.stream().map(s -> s == null ? 0L : s/1024L/1024L).reduce(0L, (a,b) -> a + b));
      
      String collectionOid = (String) map.get("component.oid");
      if (collectionOid != null && collectionOid.length() > 0)
      {
        try
        {
          Collection col = Collection.get(collectionOid);
          List<UasComponentIF> ancestors = col.getAncestors();
          result.organizationName = ((Site) ancestors.get(ancestors.size() - 1)).getServerOrganization().getDisplayLabel().getValue();
        }
        catch (DataNotFoundException ex) {}
      }
      
      String workflowOid = (String) map.get("workflowTask");
      if (workflowOid != null && workflowOid.length() > 0)
      {
        try
        {
          WorkflowTask task = WorkflowTask.get(workflowOid);
          result.taskMessage = task.getMessage();
          result.processingStatus = task.getNormalizedStatus();
        }
        catch (DataNotFoundException ex) {}
      }
      
      String uavOid = (String) map.get("component.uav.oid");
      if (uavOid != null && uavOid.length() > 0)
      {
        try
        {
          UAV uav = UAV.get(uavOid);
          result.uavSerial = uav.getSerialNumber();
          result.uavFAA = uav.getFaaNumber();
        }
        catch (DataNotFoundException ex) {}
      }
      
      String sensorOid = (String) map.get("component.collectionSensor.oid");
      if (sensorOid != null && sensorOid.length() > 0)
      {
        try
        {
          Sensor sensor = Sensor.get(sensorOid);
          result.sensorName = sensor.getName();
          result.sensorType = sensor.getType();
        }
        catch (DataNotFoundException ex) {}
      }
      
      result.odmConfig = (String) map.get("config");
      ODMProcessConfiguration config = ODMProcessConfiguration.parse(result.odmConfig);
      result.featureQuality = config.getFeatureQuality().name();
      result.radiometricCalibration = config.getRadiometricCalibration().name();
      
      results.add(result);
    });
    
    return results;
  }
}
