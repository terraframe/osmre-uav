///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
///
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ModalModule } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { AccordionModule } from 'ngx-bootstrap/accordion';
import { AlertModule } from 'ngx-bootstrap/alert';

import { FileUploadModule } from 'ng2-file-upload/ng2-file-upload';
import { CustomFormsModule } from 'ng2-validation'
import { NgxPaginationModule } from 'ngx-pagination';
import { PasswordStrengthBarModule } from 'ng2-password-strength-bar';

import { SystemLogoService } from './service/system-logo.service';
import { EmailService } from './service/email.service';
import { AccountService } from './service/account.service';

import { AdminHeaderComponent } from './component/admin-header/admin-header.component';
import { AccountsComponent } from './component/account/accounts.component';
import { AccountInviteComponent } from './component/account/account-invite.component';
import { AccountInviteCompleteComponent } from './component/account/account-invite-complete.component';
import { AccountComponent, AccountResolver } from './component/account/account.component';
import { SystemLogoComponent } from './component/logo/system-logo.component';
import { SystemLogosComponent } from './component/logo/system-logos.component';
import { EmailComponent } from './component/email/email.component';
import { SystemInfoComponent } from './component/system/system-info.component';

import { AdminRoutingModule } from './admin-routing.module';

import { SharedModule } from '../shared/shared.module';

import '../rxjs-extensions';

@NgModule( {
    imports: [
        CommonModule,
        RouterModule,
        FormsModule,
        ReactiveFormsModule,
        FileUploadModule,
        NgxPaginationModule,
        PasswordStrengthBarModule,
        CustomFormsModule,
//        ModalModule,
        AlertModule,
        BsDropdownModule,
        TypeaheadModule,
        AccordionModule,        
        SharedModule,
        AdminRoutingModule
    ],
    declarations: [
        AdminHeaderComponent,
        SystemLogoComponent,
        SystemLogosComponent,
        AccountsComponent,
        AccountInviteComponent,
        AccountInviteCompleteComponent,
        AccountComponent,
        SystemLogoComponent,
        SystemLogosComponent,
        EmailComponent,
        SystemInfoComponent
    ],
    providers: [
        SystemLogoService,
        EmailService,
        AccountService
    ]
} )
export class AdminModule { }
