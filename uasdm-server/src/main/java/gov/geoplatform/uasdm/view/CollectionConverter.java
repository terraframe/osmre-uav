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
package gov.geoplatform.uasdm.view;

import java.util.List;

import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AllPrivilegeType;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.graph.CollectionMetadata;
import gov.geoplatform.uasdm.graph.Platform;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.UAV;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.CompositeComponent;
import net.geoprism.account.GeoprismActorIF;

public class CollectionConverter extends Converter<CollectionIF>
{
  public CollectionConverter()
  {
    super();
  }

  @Override
  protected CompositeComponent<CollectionIF> convert(SiteItem siteItem, CollectionIF uasComponent)
  {
    CompositeComponent<CollectionIF> component = super.convert(siteItem, uasComponent);

    CollectionIF collection = component.getComponent();

    AllPrivilegeType privilegeType = AllPrivilegeType.valueOf(siteItem.getPrivilegeType());

    collection.addPrivilegeType(privilegeType);

    return component;
  }

  @Override
  protected SiteItem convert(CollectionIF collection, boolean includeMetadata, boolean hasChildren)
  {
    SiteItem siteItem = super.convert(collection, includeMetadata, hasChildren);

    if (!collection.getPrivilegeType().isEmpty())
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

      SessionIF session = Session.getCurrentSession();

      if (session != null)
      {
        siteItem.setOwner(user.getOid().equals(session.getUser().getOid()));
      }

      siteItem.setOwnerName(userName);
      siteItem.setOwnerPhone(phoneNumber);
      siteItem.setOwnerEmail(emailAddress);
      siteItem.setMetadataUploaded(collection.getMetadataUploaded());
      siteItem.setIsLidar(collection.isLidar());

      collection.getPrimaryProduct().ifPresent(product -> {
        siteItem.setHasAllZip(product.hasAllZip());
      });

      List<? extends WorkflowTask> tasks = WorkflowTask.getTasksForComponent(collection.getOid());
      if (!tasks.isEmpty())
      {
        WorkflowTask lastTask = tasks.get(tasks.size() - 1);
        siteItem.setDateTime(Util.formatIso8601(lastTask.getLastUpdateDate(), false));
      }

    }

    // FlightMetadata fMetadata = FlightMetadata.get(uasComponent,
    // Collection.RAW, uasComponent.getFolderName() +
    // MetadataXMLGenerator.FILENAME);

    siteItem.setPilotName(collection.getPocName());

    collection.getMetadata().ifPresent(metadata -> {
      if (metadata.getCollectionDate() != null)
      {
        String date = Util.formatIso8601(metadata.getCollectionDate(), false);
        siteItem.setCollectionDate(date);
      }

      if (metadata.getCollectionEndDate() != null)
      {
        String date = Util.formatIso8601(metadata.getCollectionEndDate(), false);
        siteItem.setCollectionEndDate(date);
      }

      Sensor sensor = metadata.getSensor();
      if (sensor != null)
      {
        siteItem.setSensor(sensor.toJSON());
      }

      UAV uav = metadata.getUav();
      if (uav != null)
      {
        siteItem.setUav(uav.toJSON());

        Platform platform = uav.getPlatform();
        if (platform != null)
        {
          siteItem.setPlatform(platform.toJSON());
        }
      }
    });

    return siteItem;
  }

  @Override
  protected CompositeComponent<CollectionIF> newInstance(CollectionIF uasComponent)
  {
    CompositeComponent<CollectionIF> component = super.newInstance(uasComponent);
    component.addMetadata(new CollectionMetadata());

    return component;
  }

  protected CompositeComponent<CollectionIF> convertNew(CollectionIF uasComponent, SiteItem siteItem)
  {
    CompositeComponent<CollectionIF> component = super.convertNew(uasComponent, siteItem);

    if (siteItem.getPrivilegeType() != null && !siteItem.getPrivilegeType().trim().equals(""))
    {
      AllPrivilegeType privilegeType = AllPrivilegeType.valueOf(siteItem.getPrivilegeType().trim().toUpperCase());
      component.getComponent().addPrivilegeType(privilegeType);
    }

    return component;
  }
}
