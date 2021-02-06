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

import com.runwaysdk.business.Element;
import com.runwaysdk.business.Entity;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import net.geoprism.GeoprismActorIF;

public interface AbstractWorkflowTaskIF
{
  /**
   * Returns a label of a component associated with this task.
   * 
   * @return label of a component associated with this task.
   */
  public String getComponentLabel();
  
  /**
   * {@link AbstractWorkflowTask#createAction()}
   */
  public void createAction(String message, String type);
  
  /**
   * {@link AbstractWorkflowTask#getGeoprismUser()}
   */
  public GeoprismActorIF getGeoprismUserIF();
  
  public SingleActor getGeoprismUser();

  /**
   * {@link AbstractWorkflowTask#getMessage(String)}
   */
  public String getMessage();
  
  /**
   * {@link AbstractWorkflowTask#setMessage(String)}
   * @param value
   */
  public void setMessage(String value);
  
  /**
   * {@link AbstractWorkflowTask#getStatus(String)}
   */
  public String getStatus();
  
  /**
   * {@link AbstractWorkflowTask#setStatus(String)}
   * @param value
   */
  public void setStatus(String value);
  
  /**
   * {@link AbstractWorkflowTask#getTaskLabel(String)}
   */
  public String getTaskLabel();

  /**
   * {@link Entity#apply()}
   */
  public void apply();
  
  /**
   * {@link Element#lock()}
   */
  public void lock();
}
