package com.runwaysdk.build.domain;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.cache.globalcache.ehcache.CacheShutdown;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.CollectionMetadata;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.service.IndexService;

public class CollectionMetadataPatch implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(CollectionMetadataPatch.class);

  public static void main(String[] args)
  {
    try
    {
      new CollectionMetadataPatch().run();
    }
    finally
    {
      if (args.length > 0 && Boolean.valueOf(args[0]))
      {
        IndexService.shutdown();
        CollectionReportFacade.finish();
        CacheShutdown.shutdown();
      }
    }

  }

  public static void start()
  {
    Thread t = new Thread(new CollectionMetadataPatch(), "CollectionMetadataPatch");
    t.setDaemon(true);
    t.start();
  }

  @Request
  public void run()
  {
    processCollections();

  }

  protected void processCollections()
  {
    List<Collection> collections = this.getCollections();

    logger.error("Patcher will migrate [" + collections.size() + "] existing products to allow for multiple products.");

    for (Collection collection : collections)
    {
      processCollection(collection);
    }

    logger.error("Finished");
  }

  @Transaction
  private void processCollection(Collection collection)
  {
    // Create the new collection metadata
    CollectionMetadata metadata = new CollectionMetadata();
    metadata.setAcquisitionDateEnd(collection.getAcquisitionDateEnd());
    metadata.setAcquisitionDateStart(collection.getAcquisitionDateStart());
    metadata.setAreaCovered(collection.getAreaCovered());
    metadata.setCollectionDate(collection.getCollectionDate());
    metadata.setCollectionEndDate(collection.getCollectionEndDate());
    metadata.setEastBound(collection.getEastBound());
    metadata.setExifIncluded(collection.getExifIncluded());
    metadata.setFlyingHeight(collection.getFlyingHeight());
    metadata.setNorthBound(collection.getNorthBound());
    metadata.setNumberOfFlights(collection.getNumberOfFlights());
    metadata.setPercentEndLap(collection.getPercentEndLap());
    metadata.setPercentSideLap(collection.getPercentSideLap());
    metadata.setSensor(collection.getSensor());
    metadata.setSouthBound(collection.getSouthBound());
    metadata.setUav(collection.getUav());
    metadata.setWeatherConditions(collection.getWeatherConditions());
    metadata.setWestBound(collection.getWestBound());
    metadata.applyWithCollection(collection);

    collection.getProducts().forEach(product -> {
      product.addChild(metadata, EdgeType.PRODUCT_HAS_METADATA).apply();
    });
    
    collection.regenerateMetadata();

    logger.error("Metadata created for collection [" + collection.getS3location() + "]");
  }

  private List<Collection> getCollections()
  {
    final String collection0 = MdVertexDAO.getMdVertexDAO(Collection.CLASS).getDBClassName();
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COLLECTION_HAS_METADATA);

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + collection0);
    builder.append(" WHERE out('" + mdEdge.getDBClassName() + "').size() = 0");

    final GraphQuery<Collection> query = new GraphQuery<Collection>(builder.toString());

    return query.getResults();
  }

}
