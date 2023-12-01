package gov.geoplatform.uasdm.service.business;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationQuery;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.OrganizationBusinessService;
import net.geoprism.registry.service.business.OrganizationBusinessServiceIF;

@Service
@Primary
public class IDMOrganizationBusinessService extends OrganizationBusinessService implements OrganizationBusinessServiceIF
{

  public List<ServerOrganization> search(String text)
  {
    OrganizationQuery query = new OrganizationQuery(new QueryFactory());
    query.WHERE(query.getDisplayLabel().localize().LIKEi("%" + text + "%"));
    query.OR(query.getCode().LIKEi("%" + text + "%"));

    try (OIterator<? extends Organization> iterator = query.getIterator())
    {
      return iterator.getAll().stream().map(org -> ServerOrganization.get(org)).collect(Collectors.toList());
    }
  }

}
