package gov.geoplatform.uasdm.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import gov.geoplatform.uasdm.AppProperties;
import net.geoprism.registry.service.LoginBruteForceGuardService;
import net.geoprism.security.UnauthorizedAccessException;

@Service
@Primary
public class UASDMLoginGuard extends LoginBruteForceGuardService
{
  @Override
  public void guardLogin()
  {
    if (AppProperties.requireKeycloakLogin())
    {
      throw new UnauthorizedAccessException();
    }
    
    super.guardLogin();
  }
}
