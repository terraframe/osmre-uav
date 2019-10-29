package com.runwaysdk.patcher.domain;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

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

public class GraphMigration
{
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
      dest.setValue(Document.OID, source.getOid());
      dest.setName(source.getName());
      dest.setS3location(source.getS3location());
    }

    @Override
    protected void persist(gov.geoplatform.uasdm.graph.Document dest, Document source)
    {
      dest.apply(source.getComponent());
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
      dest.setValue(Product.OID, source.getOid());
      dest.setName(source.getName());
      dest.setBoundingBox(source.getBoundingBox());
      dest.setLastUpdateDate(source.getLastUpdateDate());
    }

    @Override
    protected void persist(gov.geoplatform.uasdm.graph.Product dest, Product source)
    {
      dest.apply(source.getComponent());
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
      dest.setValue(UasComponent.OID, source.getOid());
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
      dest.applyWithParent(source.getParent());
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
      dest.setValue(Collection.IMAGEHEIGHT, source.getValue(Collection.IMAGEHEIGHT));
      dest.setValue(Collection.IMAGEWIDTH, source.getValue(Collection.IMAGEWIDTH));
      dest.setValue(Collection.METADATAUPLOADED, source.getValue(Collection.METADATAUPLOADED));
      dest.setValue(Collection.PLATFORM, source.getValue(Collection.PLATFORM));
      dest.setValue(Collection.PRIVILEGETYPE, source.getValue(Collection.PRIVILEGETYPE));
      dest.setValue(Collection.SENSOR, source.getValue(Collection.SENSOR));

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
      dest.setValue(Imagery.IMAGEHEIGHT, source.getValue(Imagery.IMAGEHEIGHT));
      dest.setValue(Imagery.IMAGEWIDTH, source.getValue(Imagery.IMAGEWIDTH));

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

  @Transaction
  public static void migrate()
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
