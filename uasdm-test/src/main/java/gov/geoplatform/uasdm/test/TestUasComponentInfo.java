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
package gov.geoplatform.uasdm.test;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import org.apache.commons.lang.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;

abstract public class TestUasComponentInfo
{
  protected String name;
  
  protected String folderName;
  
  protected String description;
  
  protected String geoPoint;
  
  protected String s3Location;
  
  protected List<UasComponent> children = new LinkedList<UasComponent>();
  
  protected boolean isNew = true;
  
  public TestUasComponentInfo(String name, String folderName, String s3Location, String geoPoint)
  {
    this.name = name;
    this.folderName = folderName;
    this.geoPoint = geoPoint;
    
    if (!s3Location.endsWith("/"))
      s3Location = s3Location + "/";
    
    this.s3Location = s3Location;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getFolderName()
  {
    return folderName;
  }

  public void setFolderName(String folderName)
  {
    this.folderName = folderName;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }
  
  public String getWkt()
  {
    return this.geoPoint;
  }

  public Point getGeoPoint()
  {
    if (this.getWkt() == null) { return null; }
    
    try
    {
      final WKTReader reader = new WKTReader(new GeometryFactory());

      Geometry geometry = reader.read(this.getWkt());
      geometry.setSRID(4326);
      return (Point) geometry;
    }
    catch (ParseException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public void setGeoPoint(String wkt)
  {
    this.geoPoint = wkt;
  }
  
  public String getS3location(TestProductInfo product, String folderOrFilename)
  {
    if (StringUtils.isBlank(folderOrFilename)) folderOrFilename = ImageryComponent.RAW;
    
    String ending = "";
    if (!folderOrFilename.contains(".")) ending = "/";
    
    if (product == null) {
      return this.getS3location() + folderOrFilename + ending;
    }

    return this.getS3location() + product.getS3location() + folderOrFilename + ending;
  }

  public String getS3location()
  {
    return s3Location;
  }

  public void setS3location(String s3Location)
  {
    this.s3Location = s3Location;
  }
  
  public UasComponent getServerObject()
  {
    return TestDataSet.getComponent(this.getName());
  }

  public void apply()
  {
    this.apply(null);
  }
  
  /**
   * Applies the Component which is represented by this test data into the
   * database.
   */
  @Request
  public UasComponent apply(TestUasComponentInfo parent)
  {
    UasComponent component = applyInTrans(parent);

    this.isNew = false;
    
    return component;
  }

  @Transaction
  private UasComponent applyInTrans(TestUasComponentInfo parent)
  {
    UasComponent component = this.instantiate();
    
    this.populate(component);
    
    if (parent != null)
    {
      component.applyWithParent(parent.getServerObject());
    }
    else
    {
      component.apply();
    }
    
    return component;
  }
  
  /**
   * Creates a new instance of the server object type.
   */
  abstract public UasComponent instantiate();
  
  /**
   * Populates the component with the values contained within this wrapper
   */
  public void populate(UasComponent component)
  {
    component.setName(this.getName());
    
    component.setFolderName(this.getFolderName());
    
    component.setS3location(this.getS3location());
    
    component.setDescription(this.getDescription());
    
    component.setGeoPoint(this.getGeoPoint());
  }
  
  /**
   * Cleans up all data in the database which is used to represent this
   * GeoObject. If the
   * 
   * @postcondition Subsequent calls to this.getGeoEntity will return null
   * @postcondition Subsequent calls to this.getBusiness will return null
   */
  @Request
  public void delete()
  {
    deleteInTrans();
  }

  @Transaction
  private void deleteInTrans()
  {
    UasComponent component = this.getServerObject();
    
    if (component != null)
    {
      component.delete();
    }

    this.children.clear();

    this.isNew = true;
  }
}
