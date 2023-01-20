import { NgModule } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule } from '@angular/router';

import { LoginComponent } from './core/component/login/login.component'
import { ForgotPasswordComponent } from './core/component/forgotpassword/forgotpassword.component'
import { ForgotPasswordCompleteComponent } from './core/component/forgotpassword-complete/forgotpassword-complete.component'
import { HubComponent } from './core/component/hub/hub.component';

const routes: Routes = [
    {
        path: 'menu',
        redirectTo: '/site/viewer',
    },
    {
        path: 'login',
        component: LoginComponent,
        data: { title: 'login.title' }
    },
    {
        path: 'login/:errorMsg',
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
        path: "admin",
        loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule)
    },
    {
        path: "site",
        loadChildren: () => import('./site/site.module').then(m => m.SiteModule)
    },
    {
        path: '**',
        redirectTo: '/site/viewer',
    }
];

@NgModule( {
    imports: [RouterModule.forRoot( routes )],
    exports: [RouterModule],
    providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }]
} )
export class UasdmAppRoutingModule { }

export const routedComponents = [LoginComponent, HubComponent, ForgotPasswordComponent, ForgotPasswordCompleteComponent];
