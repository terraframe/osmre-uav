/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.bus;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.runwaysdk.Pair;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.metadata.MdBusiness;

import gov.geoplatform.uasdm.command.IndexCreateDocumentCommand;
import gov.geoplatform.uasdm.command.IndexDeleteDocumentCommand;
import gov.geoplatform.uasdm.command.IndexDeleteDocumentsCommand;
import gov.geoplatform.uasdm.command.IndexUpdateDocumentCommand;
import gov.geoplatform.uasdm.command.RemoteFileDeleteCommand;
import gov.geoplatform.uasdm.model.ComponentWithAttributes;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.view.AdminCondition;
import gov.geoplatform.uasdm.view.Artifact;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.ComponentProductDTO;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

public abstract class UasComponent extends UasComponentBase implements UasComponentIF
{
  private static final long serialVersionUID = -2027002868;

  public UasComponent()
  {
    super();
  }

  /**
   * There will be a default child type for each node.
   * 
   * @return a new {@link UasComponent} of the correct type.
   */
  public abstract UasComponent createDefaultChild();

  @Override
  public List<Pair<ComponentWithAttributes, List<AttributeType>>> getCompositeAttributes()
  {
    return new LinkedList<>();
  }

  /**
   * Create the child of the given type.
   * 
   * @param return
   *          the child of the given type. It assumes the type is valid. It is
   *          the type name of the Runway {@link MdBusiness}.
   * 
   * @return a new {@link UasComponent} of the correct type.
   */
  public UasComponent createChild(String typeName)
  {
    return this.createDefaultChild();
  }

  /**
   * @return The name of the solr field for the components id.
   */
  public abstract String getSolrIdField();

  /**
   * @return The name of the solr field for the components name.
   */
  public abstract String getSolrNameField();

  /**
   * Creates the object and builds the relationship with the parent.
   * 
   * Creates directory in S3.
   * 
   * @param parent
   */
  @Transaction
  public void applyWithParent(UasComponentIF parent)
  {
    boolean isNew = this.isNew();

    if (isNew)
    {
      this.setFolderName(UUID.randomUUID().toString().replaceAll("-", ""));
    }

    if (this.isModified(UasComponent.FOLDERNAME))
    {
      String name = this.getFolderName();

      if (!UasComponentIF.isValidName(name))
      {
        MdBusinessDAOIF mdBusiness = MdBusinessDAO.getMdBusinessDAO(UasComponent.CLASS);
        MdAttributeConcreteDAOIF mdAttribute = mdBusiness.definesAttribute(UasComponent.FOLDERNAME);

        InvalidUasComponentNameException ex = new InvalidUasComponentNameException("The folder name field has an invalid character");
        ex.setAttributeName(mdAttribute.getDisplayLabel(Session.getCurrentLocale()));
        throw ex;
      }
    }

    if (isNew)
    {
      if (parent != null)
      {
        boolean isDuplicate = isDuplicateFolderName(parent.getOid(), this.getOid(), this.getFolderName());

        if (isDuplicate)
        {
          DuplicateComponentException e = new DuplicateComponentException();
          e.setParentName(parent.getName());
          e.setChildComponentLabel(this.getMdClass().getDisplayLabel(Session.getCurrentLocale()));
          e.setChildName(this.getFolderName());

          throw e;
        }
      }

      String key;

      if (parent != null)
      {
        key = this.buildS3Key(parent);
      }
      else
      {
        key = this.buildS3Key(null);
      }

      this.setS3location(key);

      this.createS3Folder(key);
    }

    this.apply();

    if (parent != null)
    {
      this.addComponent((UasComponent) parent).apply();
    }

    if (isNew)
    {
      new IndexCreateDocumentCommand(this.getAncestors(), this).doIt();
    }
  }

  @Override
  public void apply()
  {
    boolean isNameModified = this.isModified(UasComponent.NAME);
    boolean needsUpdate = this.needsUpdate();
    boolean isNew = this.isNew();

    super.apply();

    if (!isNew)
    {
      if (isNameModified || needsUpdate)
      {
        new IndexUpdateDocumentCommand(this, isNameModified).doIt();
      }
    }
  }

  protected boolean needsUpdate()
  {
    return this.isModified(UasComponent.DESCRIPTION);
  }

  @Transaction
  public void delete()
  {
    List<AbstractWorkflowTask> tasks = this.getTasks();

    for (AbstractWorkflowTask task : tasks)
    {
      task.delete();
    }

    // Delete all of the products
    List<Product> products = this.getProducts();

    for (Product product : products)
    {
      product.delete();
    }

    // Delete all of the documents
    List<DocumentIF> documents = this.getDocuments();

    for (DocumentIF document : documents)
    {
      document.delete();
    }

    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.getS3location(), null);
    }

    final IndexDeleteDocumentsCommand command = new IndexDeleteDocumentsCommand(this.getSolrIdField(), this.getOid());
    command.doIt();
  }

  /**
   * Builds a key for S3 that conforms to the directory structure requirements.
   * If the parent is null, then
   * 
   * @param uasComponent
   *          null if no parent
   * @return a key for S3 that conforms to the directory structure requirements.
   */
  public String buildS3Key(UasComponentIF parent)
  {
    String key = new String();

    if (parent != null)
    {
      key += parent.getS3location();
    }

    key += this.getFolderName() + "/";

    return key;
  }

  protected void createS3Folder(String key)
  {
    RemoteFileFacade.createFolder(key);
  }

  protected void deleteS3Folder(String key, String folderName)
  {
    new RemoteFileDeleteCommand(key, this).doIt();
  }

  public static boolean isDuplicateFolderName(String parentId, String oid, String folderName)
  {
    QueryFactory qf = new QueryFactory();
    UasComponentQuery childQ = new UasComponentQuery(qf);

    UasComponentQuery parentQ = new UasComponentQuery(qf);
    parentQ.WHERE(parentQ.getOid().EQ(parentId));

    childQ.WHERE(childQ.getFolderName().EQ(folderName));
    childQ.AND(childQ.component(parentQ));

    if (oid != null)
    {
      childQ.AND(childQ.getOid().NE(oid));
    }

    try (OIterator<? extends UasComponent> i = childQ.getIterator())
    {
      if (i.hasNext())
      {
        return true;
      }
    }

    return false;
  }

  @Override
  public JSONArray getArtifacts()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Artifact[] getArtifactObjects(ProductIF product)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeArtifacts(ProductIF product, String folder, boolean updateMetadata)
  {
    throw new UnsupportedOperationException();
  }

  public SiteObjectsResultSet getSiteObjects(String folder, Long pageNumber, Long pageSize)
  {
    return new SiteObjectsResultSet(0L, pageNumber, pageSize, new LinkedList<SiteObject>(), folder);
  }

  protected SiteObjectsResultSet getSiteObjects(String folder, List<SiteObject> objects, Long pageNumber, Long pageSize)
  {
    return RemoteFileFacade.getSiteObjects(this, folder, objects, pageNumber, pageSize);
  }

  @Override
  public Optional<ProductIF> getProduct(String productName)
  {
    return Optional.empty();
  }

  @Transaction
  public void deleteObject(String key)
  {
    final Document document = Document.find(key);
    document.delete();

    new IndexDeleteDocumentCommand(this, key).doIt();
  }

  public RemoteFileObject download(String key)
  {
    return RemoteFileFacade.download(key);
  }

  @Override
  public RemoteFileObject download(String key, List<Range> ranges)
  {
    return RemoteFileFacade.download(key, ranges);
  }

  public int getItemCount(String key)
  {
    return RemoteFileFacade.getItemCount(key);
  }
  
  public List<UasComponentIF> getAncestors()
  {
    return getAncestors(true);
  }

  public List<UasComponentIF> getAncestors(boolean filter)
  {
    List<UasComponentIF> ancestors = new LinkedList<UasComponentIF>();

    List<UasComponent> parents = this.getParents();

    ancestors.addAll(parents);

    for (UasComponent parent : parents)
    {
      ancestors.addAll(parent.getAncestors());
    }

    return ancestors;
  }

  public List<UasComponent> getParents()
  {
    List<UasComponent> parents = new LinkedList<>();

    OIterator<? extends UasComponent> it = this.getAllComponent();

    try
    {
      parents.addAll(it.getAll());
    }
    finally
    {
      it.close();
    }

    return parents;
  }

  public UasComponent getParent()
  {
    final List<UasComponent> parents = this.getParents();

    if (parents.size() > 0)
    {
      return parents.get(0);
    }

    return null;
  }

  public List<AttributeType> attributes()
  {
    List<AttributeType> list = new LinkedList<AttributeType>();
    list.add(AttributeType.create(this.getMdAttributeDAO(UasComponent.NAME)));
    list.add(AttributeType.create(this.getMdAttributeDAO(UasComponent.FOLDERNAME), true, new AdminCondition()));
    list.add(AttributeType.create(this.getMdAttributeDAO(UasComponent.DESCRIPTION)));

    return list;
  }

  public void writeFeature(JSONWriter writer) throws IOException
  {
    writer.object();

    writer.key("type");
    writer.value("Feature");

    writer.key("properties");
    writer.value(this.getProperties());

    writer.key("id");
    writer.value(this.getOid());

    writer.key("geometry");
    writer.value(new GeoJsonWriter().write(this.getGeoPoint()));

    writer.endObject();
  }

  public JSONObject getProperties()
  {
    JSONObject properties = new JSONObject();
    properties.put("oid", this.getOid());
    properties.put("name", this.getName());

    return properties;
  }

  public static JSONObject features() throws IOException
  {
    StringWriter sWriter = new StringWriter();
    JSONWriter writer = new JSONWriter(sWriter);

    SiteQuery query = new SiteQuery(new QueryFactory());
    query.WHERE(query.getGeoPoint().NE((String) null));

    try (OIterator<? extends Site> it = query.getIterator())
    {
      writer.object();

      writer.key("type");
      writer.value("FeatureCollection");
      writer.key("features");
      writer.array();

      while (it.hasNext())
      {
        Site site = it.next();

        if (site.getGeoPoint() != null)
        {
          site.writeFeature(writer);
        }
      }

      writer.endArray();

      writer.key("totalFeatures");
      writer.value(query.getCount());

      writer.key("crs");
      writer.value(new JSONObject("{\"type\":\"name\",\"properties\":{\"name\":\"urn:ogc:def:crs:EPSG::4326\"}}"));

      writer.endObject();
    }

    return new JSONObject(sWriter.toString());
  }

  public static JSONArray bbox()
  {
    try
    {
      MdBusinessDAOIF mdBusiness = MdBusinessDAO.getMdBusinessDAO(UasComponent.CLASS);
      MdAttributeConcreteDAOIF mdAttribute = mdBusiness.definesAttribute(UasComponent.GEOPOINT);

      StringBuffer sql = new StringBuffer();
      sql.append("SELECT ST_AsText(ST_Extent(" + mdAttribute.getColumnName() + ")) AS bbox");
      sql.append(" FROM " + mdBusiness.getTableName());
      sql.append(" WHERE " + mdAttribute.getColumnName() + " IS NOT NULL");

      try (ResultSet resultSet = Database.query(sql.toString()))
      {
        if (resultSet.next())
        {
          String bbox = resultSet.getString("bbox");

          if (bbox != null)
          {
            Pattern p = Pattern.compile("POLYGON\\(\\((.*)\\)\\)");
            Matcher m = p.matcher(bbox);

            if (m.matches())
            {
              String coordinates = m.group(1);
              List<Coordinate> coords = new LinkedList<Coordinate>();

              for (String c : coordinates.split(","))
              {
                String[] xAndY = c.split(" ");
                double x = Double.valueOf(xAndY[0]);
                double y = Double.valueOf(xAndY[1]);

                coords.add(new Coordinate(x, y));
              }

              Envelope e = new Envelope(coords.get(0), coords.get(2));

              JSONArray bboxArr = new JSONArray();
              bboxArr.put(e.getMinX());
              bboxArr.put(e.getMinY());
              bboxArr.put(e.getMaxX());
              bboxArr.put(e.getMaxY());

              return bboxArr;
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      // Ignore the error and just return the default bounding box
    }

    // Extent of the continental United States
    JSONArray bboxArr = new JSONArray();
    bboxArr.put(-125.0011);
    bboxArr.put(24.9493);
    bboxArr.put(-66.9326);
    bboxArr.put(49.5904);

    return bboxArr;
  }

  @Override
  public DocumentIF createDocumentIfNotExist(String key, String name, DocumentIF.Metadata metadata)
  {
    return Document.createIfNotExist(this, key, name);
  }

  @Override
  public ProductIF createProductIfNotExist(String productName)
  {
    return Product.createIfNotExist(this);
  }

  /**
   * Returns tasks associated with this item.
   * 
   * @return tasks associated with this item.
   */
  public abstract List<AbstractWorkflowTask> getTasks();

  @Override
  public AbstractWorkflowTask createWorkflowTask(String uploadId, String uploadTarget)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public DocumentIF putFile(String folder, String fileName, ProductIF product, RemoteFileMetadata metadata, InputStream stream)
  {
    throw new UnsupportedOperationException();
  }

  public List<String> uploadArchive(AbstractWorkflowTask task, ApplicationFileResource archive, String uploadTarget, ProductIF product)
  {
    throw new UnsupportedOperationException();
  }

  public Long getNumberOfChildren()
  {
    try (OIterator<? extends UasComponent> children = this.getAllComponents())
    {
      return Long.valueOf(children.getAll().size());
    }
  }

  @Override
  public List<UasComponentIF> getChildren()
  {
    try (OIterator<? extends UasComponent> children = this.getAllComponents())
    {
      return new LinkedList<UasComponentIF>(children.getAll());
    }
  }

  @Override
  public List<UasComponentIF> getChildrenWithConditions(String conditions)
  {
    return this.getChildren();
  }

  public UasComponent getChild(String name)
  {
    QueryFactory factory = new QueryFactory();
    ComponentHasComponentQuery rQuery = new ComponentHasComponentQuery(factory);
    rQuery.WHERE(rQuery.getParent().EQ(this));

    UasComponentQuery query = new UasComponentQuery(factory);
    query.WHERE(query.getName().EQ(name));
    query.AND(query.component(rQuery));

    try (OIterator<? extends UasComponent> children = query.getIterator())
    {
      if (children.hasNext())
      {
        return children.next();
      }
    }

    return null;
  }

  public String getStoreName(String key)
  {
    String baseName = FilenameUtils.getBaseName(key);

    return this.getOid() + "-" + baseName;
  }

  public List<DocumentIF> getDocuments()
  {
    DocumentQuery query = new DocumentQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(this));

    try (OIterator<? extends Document> iterator = query.getIterator())
    {
      return new LinkedList<DocumentIF>(iterator.getAll());
    }
  }

  public List<Product> getProducts()
  {
    ProductQuery query = new ProductQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(this));

    try (OIterator<? extends Product> iterator = query.getIterator())
    {
      return new LinkedList<Product>(iterator.getAll());
    }
  }

  @Override
  public List<ComponentProductDTO> getDerivedProducts(String sortField, String sortOrder)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getS3location(ProductIF product, String folder)
  {
    return this.getS3location();
  }

  @Override
  public void setPrimaryProduct(ProductIF product)
  {
  }

  @Override
  public Optional<ProductIF> getPrimaryProduct()
  {
    return Optional.empty();
  }

  @Override
  public void removeProduct(String productName)
  {
  }

  @Override
  public boolean isPrivate()
  {
    return false;
  }
}
