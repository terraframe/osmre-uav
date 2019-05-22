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
import { FormsModule} from '@angular/forms';

import { FileUploadModule } from 'ng2-file-upload/ng2-file-upload';
import { CustomFormsModule } from 'ng2-validation'
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { ModalModule } from 'ngx-bootstrap/modal'
import { NgxPaginationModule } from 'ngx-pagination';
import { PasswordStrengthBarModule } from 'ng2-password-strength-bar';

import { CoreModule } from '../core/core.module';

import { SystemLogoService } from './logo/system-logo.service';
import { EmailService } from './email/email.service';
import { AccountService } from './account/account.service';
import { GeoTreeService } from './geotree/geotree.service';
import { UniversalTreeService } from './universaltree/universaltree.service';
import { ClassifierTreeService } from './classifiertree/classifiertree.service';
import { BrowserService } from './browser/browser.service';
import { AdminRoutingModule, routedComponents } from './admin-routing.module';
 
import { AdminHeaderComponent } from './admin-header.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    FileUploadModule,
    BsDropdownModule.forRoot(),
    ModalModule.forRoot(),    
    CustomFormsModule,
    NgxPaginationModule,    
    PasswordStrengthBarModule,
    AdminRoutingModule,
    CoreModule
  ],
  declarations: [
	// Global components
    AdminHeaderComponent,
    routedComponents
  ],
  providers: [
    SystemLogoService,
    EmailService,
    AccountService,
    GeoTreeService,
    UniversalTreeService,
    ClassifierTreeService,
    BrowserService
  ]
})
export class AdminModule { }
