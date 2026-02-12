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
package gov.geoplatform.uasdm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.runwaysdk.build.domain.PatchConfig;
import com.runwaysdk.dataaccess.cache.globalcache.ehcache.CacheShutdown;

import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.service.IndexService;
import gov.geoplatform.uasdm.service.business.KnowStacBusinessService;

public class Sandbox
{
  public static void main(String[] args) throws FileNotFoundException, IOException
  {
    try
    {
      try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PatchConfig.class))
      {
        KnowStacBusinessService service = context.getBean(KnowStacBusinessService.class);

        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(StacItem.class);

        try (FileInputStream istream = new FileInputStream("/home/jsmethie/git/osmre-uav/uasdm-server/ec03053e-2309-4211-8acb-3d4858166ecf.json"))
        {
          StacItem item = reader.readValue(istream);

          service.remove(item.getId());
          
          service.put(item);
        }
      }
    }
    finally
    {
      if (args.length > 0 && Boolean.valueOf(args[0]))
      {
        IndexService.shutdown();
        CollectionReportFacade.finish();
        CacheShutdown.shutdown();
      }
    }
  }
}
