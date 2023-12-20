///
///
///

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { AccordionModule } from 'ngx-bootstrap/accordion';
import { AlertModule } from 'ngx-bootstrap/alert';

import { FileUploadModule } from 'ng2-file-upload';
import { NgxPaginationModule } from 'ngx-pagination';

import { SystemLogoService } from './service/system-logo.service';
import { EmailService } from './service/email.service';
import { AccountService } from './service/account.service';
import { SessionEventService } from './service/session-event.service';

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

import { LPGSyncTableComponent } from './component/labeled-property-graph-sync/labeled-property-graph-sync-table.component';
import { LPGSyncComponent } from './component/labeled-property-graph-sync/labeled-property-graph-sync.component';

import { AdminRoutingModule } from './admin-routing.module';

import { SharedModule } from '../shared/shared.module';
import { OrganizationSyncTableComponent } from './component/organization-sync/organization-sync-table.component';
import { OrganizationSyncComponent } from './component/organization-sync/organization-sync.component';
import { TreeModule } from '@circlon/angular-tree-component';
import { ContextMenuModule } from '@perfectmemory/ngx-contextmenu';

@NgModule({
	imports: [
		CommonModule,
		RouterModule,
		FormsModule,
		FileUploadModule,
		NgxPaginationModule,
		AlertModule,
		BsDropdownModule,
		TypeaheadModule,
		AccordionModule,
		SharedModule,
		AdminRoutingModule,
		ContextMenuModule,
		TreeModule,
	],
	declarations: [
		SystemLogoComponent,
		SystemLogosComponent,
		AccountsComponent,
		AccountInviteComponent,
		AccountInviteCompleteComponent,
		AccountComponent,
		SystemLogoComponent,
		SystemLogosComponent,
		EmailComponent,
		SystemInfoComponent,
		SessionEventComponent,
        SystemConfigurationComponent,
        LPGSyncTableComponent,
		LPGSyncComponent,
		OrganizationSyncTableComponent,
		OrganizationSyncComponent
	],
	providers: [
		SystemLogoService,
		EmailService,
		AccountService,
		SessionEventService
	],
	entryComponents: [
	]
})
export class AdminModule { }
