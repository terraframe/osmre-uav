package com.runwaysdk.build.domain;

import java.util.ArrayList;
import java.util.List;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.odm.AllZipS3Uploader.BasicODMFile;


public class PointcloudS3Migrator
{
  public static void main(String[] args) throws InterruptedException
  {
    doIt();
  }
  
  @Request
  public static void doIt() throws InterruptedException
  {
    List<BasicODMFile> processingConfigs = new ArrayList<BasicODMFile>();
    
    processingConfigs.add(new BasicODMFile("potree_pointcloud", "odm_all/potree", new String[]{"cloud.js", "data"}, true));
    
    Product.refreshAllDocuments(processingConfigs);
  }
}
