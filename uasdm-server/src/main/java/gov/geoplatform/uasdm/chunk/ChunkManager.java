/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Runway SDK(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package gov.geoplatform.uasdm.chunk;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.geoplatform.uasdm.AppProperties;

public class ChunkManager implements Runnable
{
  private static final Log                      log           = LogFactory.getLog(ChunkManager.class);

  /**
   * Executer responsible for running the cleanup thread
   */
  private static final ScheduledExecutorService executor      = Executors.newSingleThreadScheduledExecutor();

  /**
   * Interval time in hours
   */
  public static final long                      INTERVAL_TIME = 1;

  @Override
  public void run()
  {
    try
    {
      Calendar instance = Calendar.getInstance();
      instance.setTime(new Date());
      instance.add(Calendar.DATE, ( -1 * AppProperties.getChunkExpireTime() ));

      File directory = AppProperties.getUploadDirectory();

      File[] files = directory.listFiles();

      if (files != null)
      {
        for (File file : files)
        {
          if (file.exists() && file.isDirectory())
          {
            Calendar lastModified = Calendar.getInstance();
            lastModified.setTime(new Date(file.lastModified()));

            if (instance.after(lastModified))
            {
              FileUtils.deleteQuietly(file);
            }

          }
        }
      }
    }
    catch (Exception e)
    {
      log.error(e);
    }
  }

  public static void start()
  {
    executor.scheduleWithFixedDelay(new ChunkManager(), 0, INTERVAL_TIME, TimeUnit.HOURS);
  }

  public static void stop()
  {
    executor.shutdown();
  }
}
