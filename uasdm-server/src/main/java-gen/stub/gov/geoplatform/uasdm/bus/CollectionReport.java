package gov.geoplatform.uasdm.bus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.opencsv.CSVWriter;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.session.Request;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.Selectable;
import com.runwaysdk.query.SelectableChar;
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
import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
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
    object.put(CollectionReport.PRODUCT, this.getValue(PRODUCT));
    object.put(CollectionReport.EXISTS, this.getExists());
    object.put(CollectionReport.DOWNLOADCOUNTS, this.getDownloadCounts());
    object.put(CollectionReport.DELETEDATE, Util.formatIso8601(this.getDeleteDate(), false));
    object.put(CollectionReport.CREATEDATE, Util.formatIso8601(this.getCreateDate(), false));

    Long storageSize = this.getAllStorageSize();

    if (storageSize != null)
    {
      // Convert storage size to gb
      BigDecimal size = new BigDecimal(storageSize);
      size = size.divide(new BigDecimal(Math.pow(1024, 3)), 4, RoundingMode.HALF_UP);

      object.put(CollectionReport.ALLSTORAGESIZE, size.toString() + " GB");
    }

    Point geometry = this.getGeometry();

    if (geometry != null)
    {
      object.put("siteLatDecimalDegree", geometry.getY());
      object.put("siteLongDecimalDegree", geometry.getX());
    }

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
    Sensor sensor = child.getSensor();
    Bureau bureau = site.getBureau();
    GeoprismUser owner = (GeoprismUser) collection.getOwner();

    Point geometry = site.getGeoPoint();

    CollectionReport report = new CollectionReport();
    report.setSite(site);
    report.setGeometry(geometry);
    report.setProject(project);
    report.setMission(mission);
    report.setSiteName(site.getName());
    report.setProjectName(project.getName());
    report.setMissionName(mission.getName());
    report.setCollection(collection);
    report.setCollectionDate(collection.getCollectionDate());
    report.setCollectionName(collection.getName());
    report.setErosMetadataComplete(collection.getMetadataUploaded());
    report.setProductsShared(false);
    report.setOdmProcessing("not requested");
    report.setAllStorageSize(0L);
    report.setExists(true);
    report.setDownloadCounts(0L);

    if (owner != null)
    {
      report.setActor(owner);
      report.setUserName(owner.getUsername());
    }
    else
    {
      report.setUserName("N/A");
    }

    if (bureau != null)
    {
      report.setBureau(bureau);
      report.setBureauName(bureau.getName());
    }
    else
    {
      report.setBureauName("N/A");
    }

    if (uav != null)
    {
      report.setUav(uav);
      report.setFaaIdNumber(uav.getFaaNumber());
      report.setSerialNumber(uav.getSerialNumber());

      Platform platform = uav.getPlatform();

      if (platform != null)
      {
        report.setPlatform(platform);
        report.setPlatformName(platform.getName());
      }
    }
    else
    {
      report.setFaaIdNumber("N/A");
      report.setSerialNumber("N/A");
      report.setPlatformName("N/A");
    }

    if (sensor != null)
    {
      report.setSensor(sensor);
      report.setSensorName(sensor.getName());
    }
    else
    {
      report.setSensorName("N/A");
    }

    if (collection != null)
    {
      List<Product> products = collection.getProducts();

      if (products.size() > 0)
      {
        Product product = products.get(0);

        report.setProduct(product);
        report.setProductsShared(product.getPublished());
        report.setOdmProcessing(ODMStatus.COMPLETED.getLabel());
      }
      else
      {
        report.setProductsShared(false);

        List<AbstractWorkflowTask> tasks = collection.getTasks();
        long count = tasks.stream().filter(t -> ( tasks instanceof ODMProcessingTask )).count();

        if (count > 0)
        {
          report.setOdmProcessing(ODMStatus.FAILED.getLabel());
        }
        else
        {
          report.setOdmProcessing("not requested");
        }
      }

      List<DocumentIF> documents = collection.getDocuments();
      List<DocumentIF> rawDocuments = documents.stream().filter(d -> d.getS3location().contains("/" + Collection.RAW + "/")).collect(Collectors.toList());
      List<DocumentIF> videoDocuments = documents.stream().filter(d -> d.getS3location().contains("/" + Collection.VIDEO + "/")).collect(Collectors.toList());
      List<DocumentIF> orthoDocuments = documents.stream().filter(d -> d.getS3location().contains("/" + Collection.ORTHO + "/")).collect(Collectors.toList());
      List<DocumentIF> pointCloudDocuments = documents.stream().filter(d -> d.getS3location().contains("/" + Collection.PTCLOUD + "/")).collect(Collectors.toList());
      List<DocumentIF> demDocuments = documents.stream().filter(d -> d.getS3location().contains("/" + Collection.DEM + "/")).collect(Collectors.toList());

      report.setRawImagesCount(rawDocuments.size());
      report.setVideo(videoDocuments.size() > 0);
      report.setOrthomosaic(orthoDocuments.size() > 0);
      report.setPointCloud(pointCloudDocuments.size() > 0);
      report.setHillshade(demDocuments.size() > 0);
    }

    report.apply();
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

  public static List<CollectionReport> getForCollection(CollectionIF collection)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getCollection().EQ(collection.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      return new LinkedList<CollectionReport>(iterator.getAll());
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
        CollectionReport report = iterator.next();
        report.appLock();
        report.setSiteName(site.getName());
        report.setGeometry(site.getGeoPoint());
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

  public static void updateIncludeSize(CollectionIF collection)
  {
    Long storageSize = RemoteFileFacade.calculateSize(collection);

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

  public static void updateSize(CollectionIF collection)
  {
    Long storageSize = RemoteFileFacade.calculateSize(collection);

    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getCollection().EQ(collection.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setAllStorageSize(storageSize);
        report.apply();
      }
    }
  }

  public void handleDelete(Collection collection)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getCollection().EQ(collection.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setCollection(null);
        report.setMission(null);
        report.setProject(null);
        report.setSite(null);
        report.setExists(false);
        report.setDeleteDate(new Date());
        report.apply();
      }
    }
  }

  public static void handleDelete(gov.geoplatform.uasdm.graph.UAV uav)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getUav().EQ(uav.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setUav(null);
        report.apply();
      }
    }
  }

  public static void handleDelete(Product product)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getProduct().EQ(product.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setProductsShared(false);
        report.setProduct(null);
        report.apply();
      }
    }
  }

  public static void handleDelete(GeoprismUser actor)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getActor().EQ(actor.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setActor(null);
        report.apply();
      }
    }
  }

  public static void handleDelete(gov.geoplatform.uasdm.graph.Sensor sensor)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getSensor().EQ(sensor.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setSensor(null);
        report.apply();
      }
    }
  }

  public static void handleDelete(gov.geoplatform.uasdm.graph.Platform platform)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getPlatform().EQ(platform.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setPlatform(null);
        report.apply();
      }
    }
  }

  public static void updateDownloadCount(CollectionIF collection)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getCollection().EQ(collection.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();
        report.appLock();
        report.setDownloadCounts(report.getDownloadCounts() != null ? ( report.getDownloadCounts() + 1 ) : 1L);
        report.apply();
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static InputStream exportCSV(JSONObject criteria)
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());

    if (criteria != null)
    {

      if (criteria.has("sortField") && criteria.has("sortOrder"))
      {
        String field = criteria.getString("sortField");
        SortOrder order = criteria.getInt("sortOrder") == 1 ? SortOrder.ASC : SortOrder.DESC;

        query.ORDER_BY(query.getS(field), order);
      }
      else if (criteria.has("multiSortMeta"))
      {
        JSONArray sorts = criteria.getJSONArray("multiSortMeta");

        for (int i = 0; i < sorts.length(); i++)
        {
          JSONObject sort = sorts.getJSONObject(i);

          String field = sort.getString("field");
          SortOrder order = sort.getInt("order") == 1 ? SortOrder.ASC : SortOrder.DESC;

          query.ORDER_BY(query.getS(field), order);
        }
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
    }

    try
    {
      final PipedInputStream istream = new PipedInputStream();
      final PipedOutputStream ostream = new PipedOutputStream(istream);

      Thread t = new Thread(new Runnable()
      {
        @Override
        @Request
        public void run()
        {
          try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(ostream)))
          {
            ArrayList<String> headers = new ArrayList<String>();
            headers.add("Collection");
            headers.add("Collection Owner");
            headers.add("Collection Date");
            headers.add("Mission");
            headers.add("Project");
            headers.add("Site");
            headers.add("Latitude");
            headers.add("Longitude");
            headers.add("Bureau");
            headers.add("Platform");
            headers.add("Sensor");
            headers.add("FAA Id Number");
            headers.add("Serial Number");
            headers.add("ODM Processing");
            headers.add("RAW Images Count");
            headers.add("EROS Metadata Complete");
            headers.add("Video");
            headers.add("Orthomosaic");
            headers.add("Point Cloud");
            headers.add("Hillshade");
            headers.add("Products Shared");
            headers.add("Storage size");
            headers.add("Number of Downloads");
            headers.add("Date of Creation");
            headers.add("Date of Delete");

            writer.writeNext(headers.toArray(new String[headers.size()]));

            try (OIterator<? extends CollectionReport> iterator = query.getIterator())
            {
              while (iterator.hasNext())
              {
                CollectionReport row = iterator.next();

                ArrayList<String> line = new ArrayList<String>();
                line.add(row.getCollectionName());
                line.add(row.getUserName());
                line.add(Util.formatIso8601(row.getCollectionDate(), false));
                line.add(row.getMissionName());
                line.add(row.getProjectName());
                line.add(row.getSiteName());

                Point geometry = row.getGeometry();

                if (geometry != null)
                {
                  line.add(Double.toString(geometry.getY()));
                  line.add(Double.toString(geometry.getX()));
                }
                else
                {
                  line.add("");
                  line.add("");
                }

                line.add(row.getBureauName());
                line.add(row.getPlatformName());
                line.add(row.getSensorName());
                line.add(row.getFaaIdNumber());
                line.add(row.getSerialNumber());
                line.add(row.getOdmProcessing());
                line.add(row.getRawImagesCount().toString());
                line.add(row.getErosMetadataComplete().toString());
                line.add(row.getVideo().toString());
                line.add(row.getOrthomosaic().toString());
                line.add(row.getPointCloud().toString());
                line.add(row.getHillshade().toString());
                line.add(row.getProductsShared().toString());
                line.add(row.getAllStorageSize().toString());
                line.add(row.getDownloadCounts().toString());
                line.add(Util.formatIso8601(row.getCreateDate(), false));
                line.add(Util.formatIso8601(row.getDeleteDate(), false));

                writer.writeNext(line.toArray(new String[line.size()]));
              }
            }
          }
          catch (IOException e)
          {
            throw new ProgrammingErrorException(e);
          }
          finally
          {
            try
            {
              ostream.close();
            }
            catch (IOException e)
            {
            }
          }
        }
      });
      t.setDaemon(true);
      t.start();

      return istream;
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
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
    else if (criteria.has("multiSortMeta"))
    {
      JSONArray sorts = criteria.getJSONArray("multiSortMeta");

      for (int i = 0; i < sorts.length(); i++)
      {
        JSONObject sort = sorts.getJSONObject(i);

        String field = sort.getString("field");
        SortOrder order = sort.getInt("order") == 1 ? SortOrder.ASC : SortOrder.DESC;

        query.ORDER_BY(query.getS(field), order);
      }
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
