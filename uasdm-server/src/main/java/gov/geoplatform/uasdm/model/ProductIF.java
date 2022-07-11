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

import java.util.Date;
import java.util.List;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.dataaccess.MdClassDAOIF;

public interface ProductIF extends ComponentIF
{
  public MdClassDAOIF getMdClass();

  public String getOid();

  public String getName();

  public String getImageKey();

  public Date getLastUpdateDate();

  public String getBoundingBox();

  public void updateBoundingBox();

  public void clear();

  public void addDocuments(List<DocumentIF> documents);

  public UasComponentIF getComponent();

  public List<DocumentIF> getGeneratedFromDocuments();

  public Page<DocumentIF> getGeneratedFromDocuments(Integer pageNumber, Integer pageSize);

  public void calculateKeys(List<UasComponentIF> components);

  public void delete();

  public void createImageService(boolean refreshMosaic);

//  public String getWorkspace();

  public boolean isPublished();

  public void togglePublished();

  public List<DocumentIF> getDocuments();

}
