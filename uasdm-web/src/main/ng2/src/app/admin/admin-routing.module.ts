import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { AuthGuard, AdminGuardService } from '../shared/service/guard.service';

import { AccountsComponent } from './component/account/accounts.component';
import { AccountInviteComponent } from './component/account/account-invite.component';
import { AccountInviteCompleteComponent } from './component/account/account-invite-complete.component';
import { AccountComponent, AccountResolver } from './component/account/account.component';
import { SystemLogoComponent } from './component/logo/system-logo.component';
import { SystemLogosComponent } from './component/logo/system-logos.component';
import { EmailComponent } from './component/email/email.component';
import { SystemInfoComponent } from './component/system/system-info.component';

const routes: Routes = [
    {
        path: 'logos',
        canActivate: [AdminGuardService],
        component: SystemLogosComponent,
        data: { title: 'System_Configuration' }
    },
    {
        path: 'logo/:oid',
        canActivate: [AdminGuardService],
        component: SystemLogoComponent,
        data: { title: 'System_Configuration' }

    },
    {
        path: 'email',
        canActivate: [AdminGuardService],
        component: EmailComponent,
        data: { title: 'System_Configuration' }

    },
    {
        path: 'accounts',
        canActivate: [AdminGuardService],
        component: AccountsComponent,
        data: { title: 'useraccounts.title' }
    },
    {
        path: 'account/:oid',
        component: AccountComponent,
        resolve: {
            account: AccountResolver
        },
        canActivate: [AdminGuardService],
        data: { title: 'account.title' }
    },
    {
        path: 'invite',
        component: AccountInviteComponent,
        data: { title: 'account.title' }
    },
    {
        path: 'invite-complete/:token',
        component: AccountInviteCompleteComponent,
        data: { title: 'account.title' }
    },
    {
        path: 'system-info',
        component: SystemInfoComponent,
        data: {}
    }
];

@NgModule( {
    imports: [RouterModule.forChild( routes )],
    exports: [RouterModule],
    providers: [
        { provide: LocationStrategy, useClass: HashLocationStrategy },
        AccountResolver
    ]
} )
export class AdminRoutingModule { }
