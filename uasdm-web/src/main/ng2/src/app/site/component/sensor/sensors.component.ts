import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { BasicConfirmModalComponent } from '../../../shared/component/modal/basic-confirm-modal.component';
import { LocalizationService } from '../../../shared/service/localization.service';

import { PageResult } from '../../../shared/model/page';
import { Sensor } from '../../model/sensor';
import { SensorService } from '../../service/sensor.service';
import { SensorComponent } from './sensor.component';

declare let acp: string;

@Component( {
    selector: 'sensors',
    templateUrl: './sensors.component.html',
    styles: ['./sensors.css']
} )
export class SensorsComponent implements OnInit {
    res: PageResult<Sensor> = {
        resultSet: [],
        count: 0,
        pageNumber: 1,
        pageSize: 10
    };
    bsModalRef: BsModalRef;
    message: string = null;

    constructor(
        private router: Router,
        private service: SensorService,
        private modalService: BsModalService,
        private localizeService: LocalizationService
    ) { }

    ngOnInit(): void {
        this.service.page( 1 ).then( res => {
            this.res = res;
        } );
    }

    remove( sensor: Sensor ): void {
        this.service.remove( sensor.oid ).then( response => {
            this.res.resultSet = this.res.resultSet.filter( h => h.oid !== sensor.oid );
        } );
    }

    onClickRemove( sensor: Sensor ): void {
        this.bsModalRef = this.modalService.show( BasicConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = "Are you sure you want to remove the sensor [" + sensor.name + "]";
        this.bsModalRef.content.submitText = "Delete";

        this.bsModalRef.content.onConfirm.subscribe( data => {
            this.remove( sensor );
        } );
    }

    edit( sensor: Sensor ): void {
        this.service.edit( sensor.oid ).then( res => {
            this.showModal( res, false );
        } );
    }

    newInstance(): void {
        this.service.newInstance().then( res => {
            this.showModal( res, true );
        } );
    }

    showModal( sensor: Sensor, newInstance: boolean ): void {
        this.bsModalRef = this.modalService.show( SensorComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.sensor = sensor;
        this.bsModalRef.content.newInstance = newInstance;

        let that = this;
        this.bsModalRef.content.onSensorChange.subscribe( data => {
            this.onPageChange( this.res.pageNumber );
        } );

    }

    onPageChange( pageNumber: number ): void {
        this.service.page( pageNumber ).then( res => {
            this.res = res;
        } );
    }
}
