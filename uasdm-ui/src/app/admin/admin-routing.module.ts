///
///
///

import { NgModule } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule } from '@angular/router';

import { AdminGuardService } from '../shared/service/guard.service';

import { SessionEventComponent } from './component/session-event/session-event.component';
import { AccountsComponent } from './component/account/accounts.component';
import { AccountInviteComponent } from './component/account/account-invite.component';
import { AccountInviteCompleteComponent } from './component/account/account-invite-complete.component';
import { AccountComponent } from './component/account/account.component';
import { SystemLogoComponent } from './component/logo/system-logo.component';
import { SystemLogosComponent } from './component/logo/system-logos.component';
import { EmailComponent } from './component/email/email.component';
import { SystemInfoComponent } from './component/system/system-info.component';
import { SystemConfigurationComponent } from './component/system/system-configuration.component';
import { LPGSyncComponent } from './component/labeled-property-graph-sync/labeled-property-graph-sync.component';
import { OrganizationSyncComponent } from './component/organization-sync/organization-sync.component';

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
        path: 'session-events',
        canActivate: [AdminGuardService],
        component: SessionEventComponent,
        data: {}
    },
    {
        path: 'lpg-sync/:oid',
        canActivate: [AdminGuardService],
        component: LPGSyncComponent,
    },
    {
        path: 'organization-sync/:oid',
        canActivate: [AdminGuardService],
        component: OrganizationSyncComponent,
    },
    {
        path: 'system-info',
        component: SystemInfoComponent,
        data: {}
    },
    {
        path: 'system-configuration',
        component: SystemConfigurationComponent,
        data: {}
    }
];

@NgModule( {
    imports: [RouterModule.forChild( routes )],
    exports: [RouterModule],
    providers: [
        { provide: LocationStrategy, useClass: HashLocationStrategy }
    ]
} )
export class AdminRoutingModule { }
