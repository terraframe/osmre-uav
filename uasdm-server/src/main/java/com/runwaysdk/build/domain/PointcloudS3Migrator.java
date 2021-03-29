package com.runwaysdk.build.domain;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Product;


public class PointcloudS3Migrator
{
  public static void main(String[] args) throws InterruptedException
  {
    doIt();
  }
  
  @Request
  public static void doIt() throws InterruptedException
  {
    Product.refreshAllDocuments();
  }
}
