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
package gov.geoplatform.uasdm.view;

import java.util.List;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AllPrivilegeType;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Platform;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.UAV;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import net.geoprism.GeoprismActorIF;

public class CollectionConverter extends Converter
{
  public CollectionConverter()
  {
    super();
  }

  @Override
  protected UasComponentIF convert(SiteItem siteItem, UasComponentIF uasComponent)
  {
    CollectionIF collection = (CollectionIF) super.convert(siteItem, uasComponent);

    AllPrivilegeType privilegeType = AllPrivilegeType.valueOf(siteItem.getPrivilegeType());

    collection.addPrivilegeType(privilegeType);

    return collection;
  }

  @Override
  protected SiteItem convert(UasComponentIF uasComponent, boolean metadata, boolean hasChildren)
  {
    SiteItem siteItem = super.convert(uasComponent, metadata, hasChildren);

    CollectionIF collection = (CollectionIF) uasComponent;

    if (collection.getPrivilegeType().size() > 0)
    {
      AllPrivilegeType privilegeType = collection.getPrivilegeType().get(0);
      siteItem.setPrivilegeType(privilegeType.name());
    }
    else
    {
      siteItem.setPrivilegeType(AllPrivilegeType.AGENCY.name());
    }

    if (collection.getOwner() instanceof GeoprismActorIF)
    {
      GeoprismActorIF user = (GeoprismActorIF) collection.getOwner();

      // String firstName = user.getFirstName();
      // String lastName = user.getLastName();
      String userName = user.getUsername();
      String phoneNumber = user.getPhoneNumber();
      String emailAddress = user.getEmail();

      siteItem.setOwnerName(userName);
      siteItem.setOwnerPhone(phoneNumber);
      siteItem.setOwnerEmail(emailAddress);
      siteItem.setMetadataUploaded(collection.getMetadataUploaded());
      
      SiteObject allZip = ((Collection) collection).getAllZip();
      siteItem.setHasAllZip(allZip != null);

      if (collection.getCollectionDate() != null)
      {
        String date = Util.formatIso8601(collection.getCollectionDate(), false);
        siteItem.setCollectionDate(date);
      }

      List<? extends WorkflowTask> tasks = WorkflowTask.getTasksForCollection(collection.getOid());
      if (tasks.size() > 0)
      {
        WorkflowTask lastTask = tasks.get(tasks.size() - 1);
        siteItem.setDateTime(Util.formatIso8601(lastTask.getLastUpdateDate(), false));
      }

    }

    // FlightMetadata fMetadata = FlightMetadata.get(uasComponent,
    // Collection.RAW, uasComponent.getFolderName() +
    // MetadataXMLGenerator.FILENAME);

    siteItem.setPilotName( collection.getPocName());

    Sensor sensor = collection.getSensor();
    if (sensor != null)
    {
      siteItem.setSensor(sensor);
    }

    UAV uav = collection.getUav();
    if (uav != null)
    {
      siteItem.setUav(uav);

      Platform platform = uav.getPlatform();
      if (platform != null)
      {
        siteItem.setPlatform(platform);
      }
    }

    return siteItem;
  }

  protected CollectionIF convertNew(UasComponentIF uasComponent, SiteItem siteItem)
  {
    CollectionIF collection = (CollectionIF) super.convertNew(uasComponent, siteItem);

    if (siteItem.getPrivilegeType() != null && !siteItem.getPrivilegeType().trim().equals(""))
    {
      AllPrivilegeType privilegeType = AllPrivilegeType.valueOf(siteItem.getPrivilegeType().trim().toUpperCase());
      collection.addPrivilegeType(privilegeType);
    }

    return collection;
  }
}
