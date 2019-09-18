import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { LoginComponent } from './core/component/login/login.component'
import { ForgotPasswordComponent } from './core/component/forgotpassword/forgotpassword.component'
import { ForgotPasswordCompleteComponent } from './core/component/forgotpassword-complete/forgotpassword-complete.component'
import { HubComponent } from './core/component/hub/hub.component';

const routes: Routes = [
    {
        path: '',
        redirectTo: '/site/tasks',
        pathMatch: 'full'
    },
    {
        path: 'menu',
        redirectTo: '/site/tasks',
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
    {
        path: 'admin',
//        canActivate: [AdminGuardService],
        loadChildren: './admin/admin.module#AdminModule'
    },
    {
        path: 'site',
//        canActivate: [AdminGuardService],
        loadChildren: './site/site.module#SiteModule'
    }
];

@NgModule( {
    imports: [RouterModule.forRoot( routes )],
    exports: [RouterModule],
    providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }]
} )
export class UasdmAppRoutingModule { }

export const routedComponents = [LoginComponent, HubComponent, ForgotPasswordComponent, ForgotPasswordCompleteComponent];
