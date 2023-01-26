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

import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { AuthService } from '@shared/service/auth.service';


@Component({
    selector: 'system-info',
    templateUrl: './system-info.component.html',
    styleUrls: ['./system-info.css']
})
export class SystemInfoComponent implements OnInit {
    
    userName: string = "";
    admin: boolean = false;

    private bsModalRef: BsModalRef;
    
    constructor(private modalService: BsModalService, private authService: AuthService) { }

    ngOnInit(): void {
        this.userName = this.authService.getUserName();
        this.admin = this.authService.isAdmin();
    }

}
