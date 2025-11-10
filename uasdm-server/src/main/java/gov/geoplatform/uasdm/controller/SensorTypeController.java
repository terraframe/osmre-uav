/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.uasdm.service.request.CrudService;
import gov.geoplatform.uasdm.service.request.SensorTypeService;

@RestController
@RequestMapping("/api/sensor-type")
public class SensorTypeController extends AbstractCrudController
{
  @Autowired
  private SensorTypeService service;

  @Override
  public CrudService getService()
  {
    return this.service;
  }

}