package gov.geoplatform.uasdm.bus;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.Selectable;
import com.runwaysdk.query.SelectableChar;
import com.runwaysdk.system.Actor;
import com.vividsolutions.jts.geom.Point;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Platform;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.UasComponentIF;
import net.geoprism.GeoprismUser;

public class CollectionReport extends CollectionReportBase implements JSONSerializable
{
  private static final long serialVersionUID = -677130973;

  public CollectionReport()
  {
    super();
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(CollectionReport.USERNAME, this.getUserName());
    object.put(CollectionReport.BUREAUNAME, this.getBureauName());
    object.put(CollectionReport.SITELATDECIMALDEGREE, this.getSiteLatDecimalDegree());
    object.put(CollectionReport.SITELONGDECIMALDEGREE, this.getSiteLongDecimalDegree());
    object.put(CollectionReport.SITENAME, this.getSiteName());
    object.put(CollectionReport.PROJECTNAME, this.getProjectName());
    object.put(CollectionReport.MISSIONNAME, this.getMissionName());
    object.put(CollectionReport.COLLECTION, this.getValue(COLLECTION));
    object.put(CollectionReport.COLLECTIONNAME, this.getCollectionName());
    object.put(CollectionReport.COLLECTIONDATE, Util.formatIso8601(this.getCollectionDate(), false));
    object.put(CollectionReport.PLATFORMNAME, this.getPlatformName());
    object.put(CollectionReport.SENSORNAME, this.getSensorName());
    object.put(CollectionReport.FAAIDNUMBER, this.getFaaIdNumber());
    object.put(CollectionReport.SERIALNUMBER, this.getSerialNumber());
    object.put(CollectionReport.ODMPROCESSING, this.getOdmProcessing());
    object.put(CollectionReport.EROSMETADATACOMPLETE, this.getErosMetadataComplete());
    object.put(CollectionReport.RAWIMAGESCOUNT, this.getRawImagesCount());
    object.put(CollectionReport.VIDEO, this.getVideo());
    object.put(CollectionReport.ORTHOMOSAIC, this.getOrthomosaic());
    object.put(CollectionReport.POINTCLOUD, this.getPointCloud());
    object.put(CollectionReport.HILLSHADE, this.getHillshade());
    object.put(CollectionReport.PRODUCTSSHARED, this.getProductsShared());
    object.put(CollectionReport.ALLSTORAGESIZE, this.getAllStorageSize());
    object.put(CollectionReport.PRODUCT, this.getValue(PRODUCT));

    return object;
  }

  @Transaction
  public static void create(CollectionIF child)
  {
    List<UasComponentIF> ancestors = child.getAncestors();
    gov.geoplatform.uasdm.graph.Site site = (gov.geoplatform.uasdm.graph.Site) ancestors.stream().filter(a -> ( a instanceof gov.geoplatform.uasdm.graph.Site )).findFirst().get();
    gov.geoplatform.uasdm.graph.Project project = (gov.geoplatform.uasdm.graph.Project) ancestors.stream().filter(a -> ( a instanceof gov.geoplatform.uasdm.graph.Project )).findFirst().get();
    gov.geoplatform.uasdm.graph.Mission mission = (gov.geoplatform.uasdm.graph.Mission) ancestors.stream().filter(a -> ( a instanceof gov.geoplatform.uasdm.graph.Mission )).findFirst().get();
    gov.geoplatform.uasdm.graph.Collection collection = (gov.geoplatform.uasdm.graph.Collection) child;
    gov.geoplatform.uasdm.graph.UAV uav = child.getUav();
    Platform platform = uav.getPlatform();
    Sensor sensor = child.getSensor();
    Bureau bureau = site.getBureau();
    GeoprismUser owner = (GeoprismUser) collection.getOwner();

    Point geometry = site.getGeoPoint();

    CollectionReport report = new CollectionReport();
    report.setActor(owner);
    report.setSite(site);
    report.setProject(project);
    report.setMission(mission);
    report.setCollection(collection);
    report.setCollectionDate(collection.getCollectionDate());
    report.setUav(uav);
    report.setPlatform(platform);
    report.setSensor(sensor);
    report.setBureau(bureau);
    report.setUserName(owner.getUsername());
    report.setBureauName(bureau.getName());
    report.setSiteLatDecimalDegree(new BigDecimal(geometry.getY()));
    report.setSiteLongDecimalDegree(new BigDecimal(geometry.getX()));
    report.setSiteName(site.getName());
    report.setProjectName(project.getName());
    report.setMissionName(mission.getName());
    report.setCollectionName(collection.getName());
    report.setPlatformName(platform.getName());
    report.setSensorName(sensor.getName());
    report.setFaaIdNumber(uav.getFaaNumber());
    report.setSerialNumber(uav.getSerialNumber());
    report.setProductsShared(false);
    report.setOdmProcessing("not requested");
    report.setAllStorageSize(0L);
    report.apply();

    /*
     * <char name="userName" required="false" label="User Name" size="4096" />
     */
  }

  @Transaction
  public static void update(UasComponentIF component)
  {
    if (component instanceof gov.geoplatform.uasdm.graph.Collection)
    {
      update((gov.geoplatform.uasdm.graph.Collection) component);
    }

    if (component instanceof gov.geoplatform.uasdm.graph.Mission)
    {
      update((gov.geoplatform.uasdm.graph.Mission) component);
    }

    if (component instanceof gov.geoplatform.uasdm.graph.Project)
    {
      update((gov.geoplatform.uasdm.graph.Project) component);
    }

    if (component instanceof gov.geoplatform.uasdm.graph.Site)
    {
      update((gov.geoplatform.uasdm.graph.Site) component);
    }
  }

  public static void update(gov.geoplatform.uasdm.graph.Collection collection)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getCollection().EQ(collection.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setCollectionName(collection.getName());
        report.setCollectionDate(collection.getCollectionDate());
        report.setErosMetadataComplete(collection.getMetadataUploaded());
        report.apply();
      }
    }
  }

  public static void update(gov.geoplatform.uasdm.graph.Mission mission)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getMission().EQ(mission.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setMissionName(mission.getName());
        report.apply();
      }
    }
  }

  public static void update(gov.geoplatform.uasdm.graph.Project project)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getProject().EQ(project.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setProjectName(project.getName());
        report.apply();
      }
    }
  }

  public static void update(gov.geoplatform.uasdm.graph.Site site)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getSite().EQ(site.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        Point point = site.getGeoPoint();

        CollectionReport report = iterator.next();
        report.appLock();
        report.setSiteName(site.getName());
        report.setSiteLatDecimalDegree(new BigDecimal(point.getY()));
        report.setSiteLongDecimalDegree(new BigDecimal(point.getX()));
        report.apply();
      }
    }
  }

  public static void update(gov.geoplatform.uasdm.graph.UAV uav)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getUav().EQ(uav.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setFaaIdNumber(uav.getFaaNumber());
        report.setSerialNumber(uav.getSerialNumber());
        report.apply();
      }
    }
  }

  public static void update(gov.geoplatform.uasdm.graph.Sensor sensor)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getSensor().EQ(sensor.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setSensorName(sensor.getName());
        report.apply();
      }
    }
  }

  public static void update(gov.geoplatform.uasdm.graph.Platform platform)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getPlatform().EQ(platform.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setPlatformName(platform.getName());
        report.apply();
      }
    }
  }

  public static void update(Collection collection, DocumentIF document)
  {
    List<DocumentIF> documents = collection.getDocuments();
    List<DocumentIF> rawDocuments = documents.stream().filter(d -> d.getS3location().contains("/" + Collection.RAW + "/")).collect(Collectors.toList());
    List<DocumentIF> videoDocuments = documents.stream().filter(d -> d.getS3location().contains("/" + Collection.VIDEO + "/")).collect(Collectors.toList());
    List<DocumentIF> orthoDocuments = documents.stream().filter(d -> d.getS3location().contains("/" + Collection.ORTHO + "/")).collect(Collectors.toList());
    List<DocumentIF> pointCloudDocuments = documents.stream().filter(d -> d.getS3location().contains("/" + Collection.PTCLOUD + "/")).collect(Collectors.toList());
    List<DocumentIF> demDocuments = documents.stream().filter(d -> d.getS3location().contains("/" + Collection.DEM + "/")).collect(Collectors.toList());

    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getCollection().EQ(collection.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setRawImagesCount(rawDocuments.size());
        report.setVideo(videoDocuments.size() > 0);
        report.setOrthomosaic(orthoDocuments.size() > 0);
        report.setPointCloud(pointCloudDocuments.size() > 0);
        report.setHillshade(demDocuments.size() > 0);
        report.apply();
      }
    }
  }

  public static void update(String component, String status)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getCollection().EQ(component));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setOdmProcessing(status);
        report.apply();
      }
    }
  }

  public static void update(Product product)
  {
    UasComponent component = product.getComponent();

    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getCollection().EQ(component.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setProductsShared(product.getPublished());
        report.setProduct(product);
        report.apply();
      }
    }
  }

  public static void update(GeoprismUser actor)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getActor().EQ(actor.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setUserName(actor.getUsername());
        report.apply();
      }
    }
  }

  public static void update(CollectionIF collection, Long storageSize)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getCollection().EQ(collection.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setCollectionName(collection.getName());
        report.setCollectionDate(collection.getCollectionDate());
        report.setErosMetadataComplete(collection.getMetadataUploaded());
        report.setAllStorageSize(storageSize);
        report.apply();
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static Page<CollectionReport> page(JSONObject criteria)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    int pageSize = 10;
    int pageNumber = 1;

    if (criteria.has("first") && criteria.has("rows"))
    {
      int first = criteria.getInt("first");
      pageSize = criteria.getInt("rows");
      pageNumber = ( first / pageSize ) + 1;

      query.restrictRows(pageSize, pageNumber);
    }

    if (criteria.has("sortField") && criteria.has("sortOrder"))
    {
      String field = criteria.getString("sortField");
      SortOrder order = criteria.getInt("sortOrder") == 1 ? SortOrder.ASC : SortOrder.DESC;

      query.ORDER_BY(query.getS(field), order);

    }

    if (criteria.has("filters"))
    {
      JSONObject filters = criteria.getJSONObject("filters");
      Iterator<String> keys = filters.keys();

      while (keys.hasNext())
      {
        String attributeName = keys.next();

        Selectable attribute = query.get(attributeName);

        if (attribute != null)
        {
          JSONObject filter = filters.getJSONObject(attributeName);

          String value = filter.get("value").toString();
          String mode = filter.get("matchMode").toString();

          if (mode.equals("contains"))
          {
            SelectableChar selectable = (SelectableChar) attribute;

            query.WHERE(selectable.LIKEi("%" + value + "%"));
          }
          else if (mode.equals("equals"))
          {
            query.WHERE(attribute.EQ(value));
          }
        }
      }
    }

    long count = query.getCount();

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      return new Page<CollectionReport>(count, pageNumber, pageSize, new LinkedList<CollectionReport>(iterator.getAll()));
    }
  }

}
