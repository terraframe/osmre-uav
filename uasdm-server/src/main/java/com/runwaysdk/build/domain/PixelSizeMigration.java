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

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Sensor;

public class PixelSizeMigration implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(PixelSizeMigration.class);

  public static void main(String[] args)
  {
    new PixelSizeMigration().run();
  }

  @Request
  public void run()
  {
    transaction();
  }

  @Transaction
  protected void transaction()
  {
    List<Sensor> sensors = Sensor.getAll();

    logger.info("Patcher will update [" + sensors.size() + "] sensors");

    for (Sensor sensor : sensors)
    {
      boolean changed = false;
      
      if (sensor.getPixelSizeHeight() != null)
      {
        sensor.setRealPixelSizeHeight(new BigDecimal(sensor.getPixelSizeHeight()));
        changed = true;
      }
      
      if (sensor.getPixelSizeWidth() != null)
      {
        sensor.setRealPixelSizeWidth(new BigDecimal(sensor.getPixelSizeWidth()));
        changed = true;
      }
      
      if (changed)
      {
        sensor.apply();
      }
    }
  }
}
