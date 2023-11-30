package gov.geoplatform.uasdm.service;

import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.service.business.IDMOrganizationBusinessService;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.request.OrganizationService;
import net.geoprism.registry.service.request.OrganizationServiceIF;

@Service
@Primary
public class IDMOrganizationService extends OrganizationService implements OrganizationServiceIF
{
  @Autowired
  private IDMOrganizationBusinessService service;

  @Request(RequestType.SESSION)
  public List<OrganizationDTO> search(String sessionId, String text)
  {
    List<ServerOrganization> organizations = this.service.search(text);

    return organizations.stream().map(org -> org.toDTO()).collect(Collectors.toList());
  }

}
