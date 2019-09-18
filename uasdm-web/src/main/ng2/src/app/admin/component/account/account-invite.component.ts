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
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/switchMap';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../../../shared/component/modal/error-modal.component';

import { Account, UserInvite } from '../../model/account';

import { AccountService } from '../../service/account.service';
import { AccountComponent } from './account.component';

@Component( {
    selector: 'account-invite',
    templateUrl: './account-invite.component.html',
    styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
} )
export class AccountInviteComponent implements OnInit {
    invite: UserInvite;

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    constructor(
        private service: AccountService,
        private location: Location,
        private modalService: BsModalService ) {
    }

    ngOnInit(): void {
        this.invite = new UserInvite();

        this.service.newInvite().then(( account: Account ) => {
            this.invite.groups = account.groups;
            this.invite.bureaus = account.bureaus;
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    cancel(): void {
        this.location.back();
    }

    onSubmit(): void {
        let roleIds: string[] = [];

        for ( let i = 0; i < this.invite.groups.length; i++ ) {
            let group = this.invite.groups[i];

            for ( let j = 0; j < group.roles.length; j++ ) {
                let role = group.roles[j];

                if ( role.assigned ) {
                    roleIds.push( role.roleId );
                }
            }
        }

        this.service.inviteUser( this.invite, roleIds ).then( response => {
            this.location.back();
        } ).catch(( err: any ) => {
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