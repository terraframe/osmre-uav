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
package gov.geoplatform.uasdm.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;

import gov.geoplatform.uasdm.bus.AllPrivilegeType;
import gov.geoplatform.uasdm.graph.CollectionMetadata;
import gov.geoplatform.uasdm.graph.RawSet;
import gov.geoplatform.uasdm.graph.Sensor.CollectionFormat;
import gov.geoplatform.uasdm.view.CreateRawSetView;

public interface CollectionIF extends UasComponentIF
{
  public void addPrivilegeType(AllPrivilegeType privilegeType);

  public List<AllPrivilegeType> getPrivilegeType();

  public Integer getImageWidth();

  public Integer getImageHeight();

  public void setMetadataUploaded(Boolean metadataUploaded);

  public Boolean getMetadataUploaded();

  public void apply();

  public JSONObject toMetadataMessage();

  public void appLock();

  public Set<String> getExcludes();

  public boolean isMultiSpectral();
  
  public boolean isRadiometric();
  
  boolean isLidar();

//  public Sensor getSensor();
//
//  public void setSensor(Sensor sensor);
//
//  public UAV getUav();
//
//  public void setUav(UAV uav);
//
//  public Date getCollectionDate();
//
//  public Date getCollectionEndDate();
//
  public void setImageHeight(Integer imageHeight);

  public void setImageWidth(Integer imageWidth);

  public String getPocName();

  public String getPocEmail();
//
//  public BigDecimal getNorthBound();
//
//  public BigDecimal getSouthBound();
//
//  public BigDecimal getEastBound();
//
//  public BigDecimal getWestBound();
//
//  public Boolean getExifIncluded();
//
//  public Date getAcquisitionDateStart();
//
//  public Date getAcquisitionDateEnd();
//
//  public Integer getFlyingHeight();
//
//  public Integer getNumberOfFlights();
//
//  public Integer getPercentEndLap();
//
//  public Integer getPercentSideLap();
//
//  public BigDecimal getAreaCovered();
//
//  public String getWeatherConditions();

  public CollectionFormat getFormat();
  
  public void setFormat(CollectionFormat format);
  
  public void setFormat(String format);
  
  public Boolean getHasAllZip();

  public void setHasAllZip(Boolean b);

  public Optional<CollectionMetadata> getMetadata();

  public RawSet createRawSetIfNotExist(CreateRawSetView view);

}
