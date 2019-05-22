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

const routes: Routes = [
    {
        path: '',
        redirectTo: '/menu',
        pathMatch: 'full'
    },
    {
	    path: 'menu',
	    component: HubComponent,
	    canActivate: [ AuthGuard ],
	    data: { title: 'login.header' }    
	},
    {
        path: 'viewer',
        component: ProjectsComponent
    },
    {
        path: 'upload',
        component: UploadComponent,
        canDeactivate: [CanDeactivateGuardService]
    },
    {
        path: 'tasks',
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
	}
];

@NgModule( {
    imports: [RouterModule.forRoot( routes )],
    exports: [RouterModule],
    providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }]
} )
export class UasdmAppRoutingModule { }

export const routedComponents = [ProjectsComponent, UploadComponent, UserProfileComponent, LoginComponent, HubComponent, ForgotPasswordComponent, ForgotPasswordCompleteComponent];
