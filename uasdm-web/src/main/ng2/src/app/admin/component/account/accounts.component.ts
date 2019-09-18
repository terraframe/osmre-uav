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
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../../../shared/component/modal/error-modal.component';
import { BasicConfirmModalComponent } from '../../../shared/component/modal/basic-confirm-modal.component';

import { User, PageResult } from '../../model/account';
import { AccountService } from '../../service/account.service';

declare let acp: string;

@Component( {
    selector: 'accounts',
    templateUrl: './accounts.component.html',
    styles: ['./accounts.css']
} )
export class AccountsComponent implements OnInit {
    res: PageResult = {
        resultSet: [],
        count: 0,
        pageNumber: 1,
        pageSize: 10
    };
    p: number = 1;

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;


    constructor( private router: Router, private service: AccountService, private modalService: BsModalService ) { }

    ngOnInit(): void {
        this.service.page( this.p ).then( res => {
            this.res = res;
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    handleDelete( user: User ): void {
        this.bsModalRef = this.modalService.show( BasicConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = 'Are you sure you want to delete the user [' + user.username + ']?';
        this.bsModalRef.content.data = user;
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = 'Delete';

        ( <BasicConfirmModalComponent>this.bsModalRef.content ).onConfirm.subscribe( data => {
            this.remove( data );
        } );
    }

    remove( user: User ): void {
        this.service.remove( user.oid ).then( response => {
            this.res.resultSet = this.res.resultSet.filter( h => h.oid !== user.oid );
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    edit( user: User ): void {
        this.router.navigate( ['/admin/account', user.oid] );
    }

    newInstance( pageNumber: number ): void {
        this.router.navigate( ['/admin/account', 'NEW'] );
    }

    onPageChange( pageNumber: number ): void {
        this.service.page( pageNumber ).then( res => {
            this.res = res;

            this.p = pageNumber;
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    inviteUsers(): void {
        this.router.navigate( ['/admin/invite'] );
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }
    }
}
