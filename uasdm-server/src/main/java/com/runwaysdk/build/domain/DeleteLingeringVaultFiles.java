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

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.VaultFile;

import gov.geoplatform.uasdm.ImageryProcessingJob;
import gov.geoplatform.uasdm.ImageryProcessingJobQuery;

public class DeleteLingeringVaultFiles
{
  private Logger logger = LoggerFactory.getLogger(DeleteLingeringVaultFiles.class);
  
  public static void main(String[] args) throws Throwable
  {
    new DeleteLingeringVaultFiles().doIt();
  }
  
  public DeleteLingeringVaultFiles()
  {
    
  }
  
  @Request
  public void doIt() throws Throwable
  {
    QueryFactory qf = new QueryFactory();
    ImageryProcessingJobQuery jobq = new ImageryProcessingJobQuery(qf);
    
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, -4);
    jobq.WHERE(jobq.getLastUpdateDate().LT(cal.getTime()));
    
    long count = 0;
    
    OIterator<? extends ImageryProcessingJob> it = jobq.getIterator();
    
    while (it.hasNext()) {
      try {
        String oid = it.next().getImageryFile();
        
        if (StringUtils.isNotBlank(oid)) {
          VaultFile.get(oid).delete();
          count++;
        }
      } catch(Throwable t) {
        logger.error("Error deleting vault file", t);
      }
    }
    
    logger.error("deleted " + count + " old vault files");
  }
}
