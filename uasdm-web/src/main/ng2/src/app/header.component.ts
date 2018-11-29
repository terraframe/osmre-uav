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
import { Component } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { ManagementService } from './management/management.service';
import { AuthService } from './auth/auth.service';


declare var acp: any;

@Component( {

    selector: 'uasdm-header',
    templateUrl: './header.component.html',
    styleUrls: []
} )
export class UasdmHeaderComponent {
    private context: string;
    private userName: string = "";
    private admin: boolean = false;


    constructor( private managementService: ManagementService, private authService: AuthService ) {
        this.context = acp;
    }

    ngOnInit(): void {

        this.userName = this.managementService.getCurrentUser();
        this.admin = this.authService.isAdmin();
    }

}
