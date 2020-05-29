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
package com.runwaysdk.build.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.AllPrivilegeType;
import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.CollectionQuery;
import gov.geoplatform.uasdm.bus.Document;
import gov.geoplatform.uasdm.bus.DocumentQuery;
import gov.geoplatform.uasdm.bus.Imagery;
import gov.geoplatform.uasdm.bus.ImageryQuery;
import gov.geoplatform.uasdm.bus.Mission;
import gov.geoplatform.uasdm.bus.MissionQuery;
import gov.geoplatform.uasdm.bus.Product;
import gov.geoplatform.uasdm.bus.ProductQuery;
import gov.geoplatform.uasdm.bus.Project;
import gov.geoplatform.uasdm.bus.ProjectQuery;
import gov.geoplatform.uasdm.bus.Site;
import gov.geoplatform.uasdm.bus.SiteQuery;
import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;

public class GraphMigration
{
  private static final Logger                                          logger     = LoggerFactory.getLogger(GraphMigration.class);

  private static Map<String, gov.geoplatform.uasdm.graph.UasComponent> COMPONENTS = new HashMap<>();

  private static Map<String, gov.geoplatform.uasdm.graph.Document>     DOCUMENTS  = new HashMap<>();

  private static Map<String, gov.geoplatform.uasdm.graph.Product>      PRODUCTS   = new HashMap<>();

  private abstract static class Converter<T, K>
  {
    public K convert(T source)
    {
      K dest = this.newInstance();

      this.populate(dest, source);

      this.persist(dest, source);

      return dest;
    }

    protected abstract void populate(K dest, T source);

    protected abstract void persist(K dest, T source);

    protected abstract K newInstance();
  }

  private static class DocumentConverter extends Converter<Document, gov.geoplatform.uasdm.graph.Document>
  {
    @Override
    protected void populate(gov.geoplatform.uasdm.graph.Document dest, Document source)
    {
      dest.getGraphObjectDAO().getAttribute(Document.OID).setValueInternal(source.getOid());
      dest.setName(source.getName());
      dest.setS3location(source.getS3location());
    }

    @Override
    protected void persist(gov.geoplatform.uasdm.graph.Document dest, Document source)
    {
      final gov.geoplatform.uasdm.graph.UasComponent component = COMPONENTS.get(source.getComponentOid());

      dest.apply(component);

      DOCUMENTS.put(source.getOid(), dest);
    }

    @Override
    protected gov.geoplatform.uasdm.graph.Document newInstance()
    {
      return new gov.geoplatform.uasdm.graph.Document();
    }
  }

  private static class ProductConverter extends Converter<Product, gov.geoplatform.uasdm.graph.Product>
  {
    @Override
    protected void populate(gov.geoplatform.uasdm.graph.Product dest, Product source)
    {
      dest.getGraphObjectDAO().getAttribute(Document.OID).setValueInternal(source.getOid());
      dest.setName(source.getName());
      dest.setBoundingBox(source.getBoundingBox());
      dest.setLastUpdateDate(source.getLastUpdateDate());
    }

    @Override
    protected void persist(gov.geoplatform.uasdm.graph.Product dest, Product source)
    {
      final gov.geoplatform.uasdm.graph.UasComponent component = COMPONENTS.get(source.getComponentOid());

      dest.apply(component);

      PRODUCTS.put(source.getOid(), dest);

      // Assign the generated documents
      final List<DocumentIF> generatedFromDocuments = source.getGeneratedFromDocuments();

      for (DocumentIF document : generatedFromDocuments)
      {
        DOCUMENTS.get(document.getOid()).addGeneratedProduct(dest);
      }

      // Assign the documents
      final List<? extends Document> docs = source.getAllDocuments().getAll();

      for (Document doc : docs)
      {
        dest.addChild(DOCUMENTS.get(doc.getOid()), EdgeType.PRODUCT_HAS_DOCUMENT).apply();
      }
    }

    @Override
    protected gov.geoplatform.uasdm.graph.Product newInstance()
    {
      return new gov.geoplatform.uasdm.graph.Product();
    }
  }

  private abstract static class UasConverter extends Converter<UasComponent, gov.geoplatform.uasdm.graph.UasComponent>
  {
    protected void populate(gov.geoplatform.uasdm.graph.UasComponent dest, UasComponent source)
    {
      dest.getGraphObjectDAO().getAttribute(Document.OID).setValueInternal(source.getOid());
      dest.setDescription(source.getDescription());
      dest.setFolderName(source.getFolderName());
      dest.setGeoPoint(source.getGeoPoint());
      dest.setName(source.getName());
      dest.setOwner(source.getOwner());
      dest.setS3location(source.getS3location());
    }

    @Override
    protected void persist(gov.geoplatform.uasdm.graph.UasComponent dest, UasComponent source)
    {
      final UasComponent p = source.getParent();

      final gov.geoplatform.uasdm.graph.UasComponent parent = p != null ? COMPONENTS.get(p.getOid()) : null;

      dest.applyWithParent(parent);

      COMPONENTS.put(source.getOid(), dest);
    }

    protected abstract gov.geoplatform.uasdm.graph.UasComponent newInstance();
  }

  private static class SiteConverter extends UasConverter
  {
    @Override
    protected void populate(gov.geoplatform.uasdm.graph.UasComponent dest, UasComponent source)
    {
      dest.setValue(Site.BUREAU, source.getValue(Site.BUREAU));
      dest.setValue(Site.OTHERBUREAUTXT, source.getValue(Site.OTHERBUREAUTXT));

      super.populate(dest, source);
    }

    @Override
    protected gov.geoplatform.uasdm.graph.UasComponent newInstance()
    {
      return new gov.geoplatform.uasdm.graph.Site();
    }
  }

  private static class ProjectConverter extends UasConverter
  {
    @Override
    protected gov.geoplatform.uasdm.graph.UasComponent newInstance()
    {
      return new gov.geoplatform.uasdm.graph.Project();
    }
  }

  private static class MissionConverter extends UasConverter
  {
    @Override
    protected gov.geoplatform.uasdm.graph.UasComponent newInstance()
    {
      return new gov.geoplatform.uasdm.graph.Mission();
    }
  }

  private static class CollectionConverter extends UasConverter
  {
    @Override
    protected void populate(gov.geoplatform.uasdm.graph.UasComponent dest, UasComponent source)
    {
      Collection cSource = (Collection) source;
      gov.geoplatform.uasdm.graph.Collection cDest = (gov.geoplatform.uasdm.graph.Collection) dest;

      dest.setValue(Collection.IMAGEHEIGHT, cSource.getImageHeight());
      dest.setValue(Collection.IMAGEWIDTH, cSource.getImageWidth());
      dest.setValue(Collection.METADATAUPLOADED, cSource.getMetadataUploaded());
      dest.setValue(Collection.PLATFORM, source.getValue(Collection.PLATFORM));
      dest.setValue(Collection.SENSOR, source.getValue(Collection.SENSOR));

      final List<AllPrivilegeType> privilegeTypes = cSource.getPrivilegeType();

      for (AllPrivilegeType privilegeType : privilegeTypes)
      {
        cDest.addPrivilegeType(privilegeType);
      }

      super.populate(dest, source);
    }

    @Override
    protected gov.geoplatform.uasdm.graph.UasComponent newInstance()
    {
      return new gov.geoplatform.uasdm.graph.Collection();
    }
  }

  private static class ImageryConverter extends UasConverter
  {
    @Override
    protected void populate(gov.geoplatform.uasdm.graph.UasComponent dest, UasComponent source)
    {
      Imagery cSource = (Imagery) source;

      dest.setValue(Collection.IMAGEHEIGHT, cSource.getImageHeight());
      dest.setValue(Collection.IMAGEWIDTH, cSource.getImageWidth());

      super.populate(dest, source);
    }

    @Override
    protected gov.geoplatform.uasdm.graph.UasComponent newInstance()
    {
      return new gov.geoplatform.uasdm.graph.Imagery();
    }
  }

  private static UasConverter factory(UasComponent uasComponent)
  {
    if (uasComponent instanceof Site)
    {
      return new SiteConverter();
    }
    else if (uasComponent instanceof Project)
    {
      return new ProjectConverter();
    }
    else if (uasComponent instanceof Mission)
    {
      return new MissionConverter();
    }
    else if (uasComponent instanceof Collection)
    {
      return new CollectionConverter();
    }
    else if (uasComponent instanceof Imagery)
    {
      return new ImageryConverter();
    }
    else
    {
      // Should never hit this case unless a new type is added to the hierarchy
      return null;
    }
  }

  public static void main(String[] args)
  {
    migrate();
  }

  @Request
  public static void migrate()
  {
    migrate_Transaction();
  }

  @Transaction
  public static void migrate_Transaction()
  {
    migrateSites();
    migrateProjects();
    migrateMissions();
    migrateCollections();
    migrateImagerys();
    migrateDocuments();
    migrateProducts();
  }

  public static void migrateSites()
  {
    SiteQuery cq = new SiteQuery(new QueryFactory());

    try (OIterator<? extends Site> it = cq.getIterator())
    {
      while (it.hasNext())
      {
        Site site = it.next();

        factory(site).convert(site);
      }
    }
  }

  public static void migrateProjects()
  {
    ProjectQuery cq = new ProjectQuery(new QueryFactory());

    try (OIterator<? extends Project> it = cq.getIterator())
    {
      while (it.hasNext())
      {
        Project site = it.next();

        factory(site).convert(site);
      }
    }
  }

  public static void migrateMissions()
  {
    MissionQuery cq = new MissionQuery(new QueryFactory());

    try (OIterator<? extends Mission> it = cq.getIterator())
    {
      while (it.hasNext())
      {
        Mission site = it.next();

        factory(site).convert(site);
      }
    }
  }

  public static void migrateCollections()
  {
    CollectionQuery cq = new CollectionQuery(new QueryFactory());

    try (OIterator<? extends Collection> it = cq.getIterator())
    {
      while (it.hasNext())
      {
        Collection site = it.next();

        factory(site).convert(site);
      }
    }
  }

  public static void migrateImagerys()
  {
    ImageryQuery cq = new ImageryQuery(new QueryFactory());

    try (OIterator<? extends Imagery> it = cq.getIterator())
    {
      while (it.hasNext())
      {
        Imagery site = it.next();

        factory(site).convert(site);
      }
    }
  }

  public static void migrateDocuments()
  {
    DocumentQuery cq = new DocumentQuery(new QueryFactory());

    try (OIterator<? extends Document> it = cq.getIterator())
    {
      while (it.hasNext())
      {
        Document site = it.next();

        new DocumentConverter().convert(site);
      }
    }
  }

  public static void migrateProducts()
  {
    ProductQuery cq = new ProductQuery(new QueryFactory());

    try (OIterator<? extends Product> it = cq.getIterator())
    {
      while (it.hasNext())
      {
        Product site = it.next();

        new ProductConverter().convert(site);
      }
    }
  }
}
