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

import { Component, EventEmitter, Input, OnInit, OnChanges, Output, Inject, ViewChild } from '@angular/core';
import { Location } from '@angular/common';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../../../shared/component/modal/error-modal.component';

import { Email } from '../../model/email';
import { EmailService } from '../../service/email.service';


@Component( {

    selector: 'email',
    templateUrl: './email.component.html',
    styleUrls: []
} )
export class EmailComponent implements OnInit {
    public email: Email = {
        oid: '',
        server: '',
        username: '',
        password: '',
        port: 0,
        from: '',
        to: '',
    };

    bsModalRef: BsModalRef;

    constructor(
        private service: EmailService,
        private modalService: BsModalService,
        private location: Location ) {
    }

    ngOnInit(): void {
        this.service.getInstance().then( email => {
            this.email = email;
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    cancel(): void {
        this.location.back();
    }

    onSubmit(): void {
        this.service.apply( this.email )
            .then( email => {
                this.location.back();
            } )
            .catch(( err: any ) => {
                this.error( err.json() );
            } );
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }
    }
}
