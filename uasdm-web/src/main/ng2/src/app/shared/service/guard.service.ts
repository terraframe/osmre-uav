import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router} from '@angular/router';
import { AuthService} from '../service/auth.service';

declare var acp: any;

@Injectable()
export class AdminGuardService implements CanActivate {

  constructor(private service:AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    
    if (this.service.isAdmin()) {
      return true; 
    }
    
    this.router.navigate([ '/profile' ]);
    
    return false;
  }
}

@Injectable()
export class AuthGuard implements CanActivate {

  constructor(private service:AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    
    if (this.service.isLoggedIn()) {
      return true; 
    }
    
    this.router.navigate([ '/login' ]);
    
    return false;
  }
}
