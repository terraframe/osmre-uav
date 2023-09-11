package gov.geoplatform.uasdm.view;

import org.springframework.lang.Nullable;

import gov.geoplatform.uasdm.UserInfo;
import net.geoprism.GeoprismUser;
import net.geoprism.account.GeoprismUserView;

public class IDMUserView extends GeoprismUserView
{
  @Nullable
  private String bureau;

  public IDMUserView(String displayName, String email, String firstName, String lastName, String phoneNumber, String oid, String bureau)
  {
    super(displayName, email, firstName, lastName, phoneNumber, oid);
    this.bureau = bureau;
  }
  
  public static IDMUserView fromUser(GeoprismUser user, UserInfo info)
  {
    // The admin user doesn't have an associated UserInfo object. So info will be null in that scenario.
    String bureau = null;
    if (info != null && info.getBureau() != null)
    {
      bureau = info.getBureau().getDisplayLabel();
    }
    
    IDMUserView view = new IDMUserView(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhoneNumber(), user.getOid(), bureau);
    return view;
  }

  public String getBureau()
  {
    return bureau;
  }

  public void setBureau(String bureau)
  {
    this.bureau = bureau;
  }
}
