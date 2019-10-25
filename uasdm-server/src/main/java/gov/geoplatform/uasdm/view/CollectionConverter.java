package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.bus.AllPrivilegeType;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import net.geoprism.GeoprismUser;

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

    if (collection.getOwner() instanceof GeoprismUser)
    {
      GeoprismUser user = (GeoprismUser) collection.getOwner();

//      String firstName = user.getFirstName();
//      String lastName = user.getLastName();
      String userName = user.getUsername();
      String phoneNumber = user.getPhoneNumber();
      String emailAddress = user.getEmail();

      siteItem.setOwnerName(userName);
      siteItem.setOwnerPhone(phoneNumber);
      siteItem.setOwnerEmail(emailAddress);
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
