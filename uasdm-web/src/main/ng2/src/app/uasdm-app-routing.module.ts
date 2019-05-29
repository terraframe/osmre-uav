import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ProjectsComponent } from './management/projects.component';
import { UploadComponent } from './management/upload.component';
import { UserProfileComponent } from './management/user-profile.component';

import { AdminGuardService } from './service/admin.guard.service';
import { CanDeactivateGuardService } from "./service/can.deactivate.guard.service";

import { LoginComponent } from './authentication/login.component'
import { ForgotPasswordComponent } from './forgotpassword/forgotpassword.component'
import { ForgotPasswordCompleteComponent } from './forgotpassword-complete/forgotpassword-complete.component'
import { AuthGuard } from './authentication/auth.guard';
import { HubComponent } from './hub/hub.component';

// TODO : Can't dynamically load the Admin Module due to a chunking error
import { AccountsComponent } from './admin/account/accounts.component';
import { AccountInviteComponent } from './admin/account/account-invite.component';
import { AccountInviteCompleteComponent } from './admin/account/account-invite-complete.component';
import { AccountComponent, AccountResolver } from './admin/account/account.component';
import { SystemLogoComponent } from './admin/logo/system-logo.component';
import { SystemLogosComponent } from './admin/logo/system-logos.component';
import { EmailComponent } from './admin/email/email.component';

import { SystemInfoComponent } from './admin/system/system-info.component';

const routes: Routes = [
    {
        path: '',
        canActivate: [ AuthGuard ],
        redirectTo: '/tasks',
        pathMatch: 'full'
    },
    {
	    path: 'menu',
	    canActivate: [ AuthGuard ],
	    redirectTo: '/tasks',
	},
    {
        path: 'viewer',
        canActivate: [ AuthGuard ],
        component: ProjectsComponent
    },
    {
        path: 'upload',
        component: UploadComponent,
        canDeactivate: [CanDeactivateGuardService]
    },
    {
        path: 'tasks',
        canActivate: [ AuthGuard ],
        component: UserProfileComponent
    },
    {
	    path: 'login',
	    component: LoginComponent,
	    data: { title: 'login.title' }    
	},
	{
	    path: 'forgotpassword',
	    component: ForgotPasswordComponent,
	    data: { title: 'useraccounts.title' }                
	},
	{
	    path: 'forgotpassword-complete/:token',
	    component: ForgotPasswordCompleteComponent
	},
//	{
//    path: 'admin',
//    canActivate: [ AdminGuardService ],
//    loadChildren: './admin/admin.module#AdminModule'    
//  }
	{
    path: 'admin/logos',
    canActivate: [ AuthGuard ],
    component: SystemLogosComponent,
    data: { title: 'System_Configuration' }                
  },
  {
    path: 'admin/logo/:oid',
    canActivate: [ AuthGuard ],
    component: SystemLogoComponent,
    data: { title: 'System_Configuration' }            
    
  },
  {
    path: 'admin/email',
    canActivate: [ AuthGuard ],
    component: EmailComponent,
    data: { title: 'System_Configuration' }            
    
  },
	{
    path: 'admin/accounts',
    canActivate: [ AuthGuard ],
    component: AccountsComponent,    
    data: { title: 'useraccounts.title' }                
  },
  {
    path: 'admin/account/:oid',
    component: AccountComponent,
    resolve: {
      account: AccountResolver
    },        
    canActivate: [ AuthGuard ],
    data: { title: 'account.title' }              
  },
  {
    path: 'admin/invite',
    component: AccountInviteComponent,    
    data: { title: 'account.title' }              
  },
  {
    path: 'admin/invite-complete/:token',
    component: AccountInviteCompleteComponent,
    data: { title: 'account.title' }              
  },
  {
    path: 'admin/system-info',
    component: SystemInfoComponent,
    data: { }
  }
];

@NgModule( {
    imports: [RouterModule.forRoot( routes )],
    exports: [RouterModule],
    providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }]
} )
export class UasdmAppRoutingModule { }

export const routedComponents = [ProjectsComponent, UploadComponent, UserProfileComponent, LoginComponent, HubComponent, ForgotPasswordComponent, ForgotPasswordCompleteComponent];
