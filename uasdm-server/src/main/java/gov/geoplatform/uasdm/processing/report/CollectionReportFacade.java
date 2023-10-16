package gov.geoplatform.uasdm.processing.report;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Platform;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.UAV;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.report.CollectionReportTask.Type;
import net.geoprism.GeoprismUser;

public class CollectionReportFacade
{
  private static CollectionReportProcessor processor = new QueuedCollectionReportProcessor();

  public static void process(CollectionReportTask task)
  {
    processor.process(task);
  }

  public static Command update(UasComponentIF component)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.UPDATE, component));
  }

  public static Command update(UAV uav)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.UAV, uav));
  }

  public static Command update(Sensor sensor)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.SENSOR, sensor));
  }

  public static Command update(Platform platform)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.PLATFORM, platform));
  }

  public static Command update(Collection collection, DocumentIF document)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.DOCUMENT, collection, document));
  }

  public static Command update(String component, String status)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.STATUS, component, status));
  }

  public static Command update(Product product)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.PRODUCT, product));
  }

  public static Command update(GeoprismUser actor)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.USER, actor));
  }

  public static Command updateIncludeSize(CollectionIF collection)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.INCLUDE_SIZE, collection));
  }

  public static Command updateSize(CollectionIF collection)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.SIZE, collection));
  }

  public static Command handleDelete(Collection collection)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.DELETE_COLLECTION, collection));
  }

  public static Command handleDelete(gov.geoplatform.uasdm.graph.UAV uav)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.DELETE_UAV, uav));
  }

  public static Command handleDelete(Product product)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.DELETE_PRODUCT, product));
  }

  public static Command handleDelete(GeoprismUser actor)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.DELETE_USER, actor));
  }

  public static Command handleDelete(gov.geoplatform.uasdm.graph.Sensor sensor)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.DELETE_SENSOR, sensor));
  }

  public static Command handleDelete(gov.geoplatform.uasdm.graph.Platform platform)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.DELETE_PLATFORM, platform));
  }

  public static Command updateDownloadCount(CollectionIF collection)
  {
    return new CollectionReportTaskCommand(new CollectionReportTask(Type.DOWNLOAD_COUNT, collection));
  }

}
