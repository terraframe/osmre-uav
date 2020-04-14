package gov.geoplatform.uasdm.remote;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.graph.Site;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;

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

    Assert.assertTrue(fService.getCreates().contains(location));
    Assert.assertTrue(fService.getDeletes().contains(location));
  }

}
