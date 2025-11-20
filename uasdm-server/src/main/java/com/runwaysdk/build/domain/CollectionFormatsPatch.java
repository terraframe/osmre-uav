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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.Sensor.CollectionFormat;

public class CollectionFormatsPatch implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(CollectionFormatsPatch.class);

  public static void main(String[] args)
  {
    new CollectionFormatsPatch().run();
  }

  public static void start()
  {
    Thread t = new Thread(new CollectionFormatsPatch(), "CollectionFormatsPatch");
    t.setDaemon(true);
    t.start();
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
      var st = sensor.getSensorType();
      
      List<CollectionFormat> formats = new ArrayList<CollectionFormat>();
      if (Boolean.TRUE.equals(st.getIsMultispectral())) {
        formats.add(CollectionFormat.STILL_MULTISPECTRAL);
        formats.add(CollectionFormat.VIDEO_MULTISPECTRAL);
      } else if (Boolean.TRUE.equals(st.getIsLidar())) {
        formats.add(CollectionFormat.LIDAR);
      } else {
        formats = Sensor.DEFAULT_FORMATS;
      }
      
      sensor.setCollectionFormats(formats);
      sensor.apply();
    }
  }
}
