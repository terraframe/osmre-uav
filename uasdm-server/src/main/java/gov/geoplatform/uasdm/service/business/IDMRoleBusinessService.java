package gov.geoplatform.uasdm.service.business;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.system.Roles;

import net.geoprism.registry.service.business.RoleBusinessService;
import net.geoprism.registry.service.business.RoleBusinessServiceIF;

@Service
@Primary
public class IDMRoleBusinessService extends RoleBusinessService implements RoleBusinessServiceIF
{
  @Override
  public List<Roles> getAllAssignableRoles()
  {
    // Exclude all roles created for imported organizations
    List<Roles> roles = super.getAllAssignableRoles();
    roles = roles.stream().filter(role -> !role.getRoleName().startsWith("cgr")).collect(Collectors.toList());

    return roles;
  }
}
