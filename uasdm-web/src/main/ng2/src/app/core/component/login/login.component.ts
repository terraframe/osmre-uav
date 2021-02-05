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
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

import { ErrorHandler, ErrorModalComponent } from '@shared/component';

import { SessionService } from '@shared/service/session.service';

import { LoginHeaderComponent } from './login-header.component';

declare var acp: any;
declare var uasdmKeycloakEnabled: boolean;

@Component( {
    selector: 'login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
} )
export class LoginComponent {
    context: string;
    username: string = '';
    password: string = '';
    
    private bsModalRef: BsModalRef;
    
    sub: Subscription;
    
    keycloakEnabled: boolean = uasdmKeycloakEnabled;

    constructor( private service: SessionService, private router: Router, private route: ActivatedRoute, private modalService: BsModalService ) {
        this.context = acp as string;
    }
    
    ngOnInit(): void {
      this.sub = this.route.params.subscribe(params => {
      
        if (params['errorMsg'] != null)
        {
          this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
          
          let encodedError = params['errorMsg'];
          let decodedError = encodedError.replaceAll("+", " ");
          
          this.bsModalRef.content.message = decodedError;
        }
      });
    }

    onClickKeycloak(): void {
      window.location.href = this.context + "/keycloak/loginRedirect";
    }

    onSubmit(): void {
      this.service.login( this.username, this.password ).then( response => {
          this.router.navigate( ['/menu/true'] );
      } );
    }
    
    public error( err: HttpErrorResponse ): void {
      this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }
}
