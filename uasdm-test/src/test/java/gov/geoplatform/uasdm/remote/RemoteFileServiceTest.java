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
package gov.geoplatform.uasdm.remote;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.graph.Site;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.remote.MockRemoteFileService.RemoteFileAction;
import gov.geoplatform.uasdm.remote.MockRemoteFileService.RemoteFileActionType;

public class RemoteFileServiceTest
{
  private MockRemoteFileService fService;

  @Before
  public void setup()
  {
    fService = new MockRemoteFileService();

    RemoteFileFacade.setService(fService);
  }

  @Test
  @Request
  public void testCreateAndDelete()
  {
    Bureau bureau = Bureau.getByKey("OSMRE");

    Site site = new Site();
    site.setValue(UasComponent.NAME, "Site_Unit_Test_1");
    site.setValue(UasComponent.FOLDERNAME, "Site_Unit_Test_1");
    site.setBureau(bureau);

    site.applyWithParent(null);
    site.delete();

    final String location = site.getS3location();

    List<RemoteFileAction> creates = fService.getActions().stream().filter(action -> action.getType().equals(RemoteFileActionType.CREATE_FOLDER)).collect(Collectors.toList());
    Assert.assertEquals(1, creates.size());
    Assert.assertTrue(creates.get(0).getKey().equals(location));
    
    List<RemoteFileAction> deletes = fService.getActions().stream().filter(action -> action.getType().equals(RemoteFileActionType.CREATE_FOLDER)).collect(Collectors.toList());
    Assert.assertEquals(1, deletes.size());
    Assert.assertTrue(deletes.get(0).getKey().equals(location));
  }

}
