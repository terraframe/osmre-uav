import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '../../../shared/component/modal/basic-confirm-modal.component';
import { LocalizationService } from '../../../shared/service/localization.service';

import { PageResult } from '../../../shared/model/page';
import { Platform } from '../../model/platform';
import { PlatformService } from '../../service/platform.service';
import { PlatformComponent } from './platform.component';

declare let acp: string;

@Component( {
    selector: 'platforms',
    templateUrl: './platforms.component.html',
    styles: ['./platforms.css']
} )
export class PlatformsComponent implements OnInit {
    res: PageResult<Platform> = {
        resultSet: [],
        count: 0,
        pageNumber: 1,
        pageSize: 10
    };
    bsModalRef: BsModalRef;
    message: string = null;

    constructor(
        private router: Router,
        private service: PlatformService,
        private modalService: BsModalService,
        private localizeService: LocalizationService
    ) { }

    ngOnInit(): void {
        this.service.page( 1 ).then( res => {
            this.res = res;
        } );
    }

    remove( platform: Platform ): void {
        this.service.remove( platform.oid ).then( response => {
            this.res.resultSet = this.res.resultSet.filter( h => h.oid !== platform.oid );
        } );
    }

    onClickRemove( platform: Platform ): void {
        this.bsModalRef = this.modalService.show( BasicConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = "Are you sure you want to remove the platform [" + platform.name + "]";
		this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = "Delete";

        this.bsModalRef.content.onConfirm.subscribe( data => {
            this.remove( platform );
        } );
    }

    edit( platform: Platform ): void {
        this.service.edit( platform.oid ).then( res => {
            this.showModal( res, false );
        } );
    }

    newInstance(): void {
        this.service.newInstance().then( res => {
            this.showModal( res, true );
        } );
    }

    showModal( platform: Platform, newInstance: boolean ): void {
        this.bsModalRef = this.modalService.show( PlatformComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.platform = platform;
        this.bsModalRef.content.newInstance = newInstance;

        let that = this;
        this.bsModalRef.content.onPlatformChange.subscribe( data => {
            this.onPageChange( this.res.pageNumber );
        } );

    }

    onPageChange( pageNumber: number ): void {
        this.service.page( pageNumber ).then( res => {
            this.res = res;
        } );
    }
}
