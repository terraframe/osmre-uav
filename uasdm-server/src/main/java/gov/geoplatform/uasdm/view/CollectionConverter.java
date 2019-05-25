package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.bus.AllPrivilegeType;
import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.UasComponent;
import net.geoprism.GeoprismUser;

public class CollectionConverter extends Converter
{
  public CollectionConverter()
  {
    super();
  }

  @Override
  protected UasComponent convert(SiteItem siteItem, UasComponent uasComponent)
  {
    Collection collection = (Collection)super.convert(siteItem, uasComponent);
    
    AllPrivilegeType privilegeType = AllPrivilegeType.valueOf(siteItem.getPrivilegeType());
    
    collection.addPrivilegeType(privilegeType);
    
    return collection;
  }

  @Override
  protected SiteItem convert(UasComponent uasComponent, boolean metadata, boolean hasChildren)
  {
    SiteItem siteItem = super.convert(uasComponent, metadata, hasChildren);
    
    Collection collection = (Collection)uasComponent;
    
    if (collection.getPrivilegeType().size() > 0)
    {
      AllPrivilegeType privilegeType = collection.getPrivilegeType().get(0);
      siteItem.setPrivilegeType(privilegeType.name());
    }
    else
    {
      collection.lock();
      collection.addPrivilegeType(AllPrivilegeType.AGENCY);
      collection.unlock();
      siteItem.setPrivilegeType(AllPrivilegeType.AGENCY.name());
    }
    
    if (collection.getOwner() instanceof GeoprismUser)
    {
      GeoprismUser user = (GeoprismUser)collection.getOwner();

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

  protected Collection convertNew(UasComponent uasComponent, SiteItem siteItem)
  {
    Collection collection = (Collection)super.convertNew(uasComponent, siteItem);
    
    if (siteItem.getPrivilegeType() != null && !siteItem.getPrivilegeType().trim().equals(""))
    {
      AllPrivilegeType privilegeType = AllPrivilegeType.valueOf(siteItem.getPrivilegeType().trim().toUpperCase());
      collection.addPrivilegeType(privilegeType);
    }
    
    return collection;
  }
}
