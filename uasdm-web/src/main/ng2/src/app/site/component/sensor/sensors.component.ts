import { Component, OnInit } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';

import { PageResult } from '@shared/model/page';
import { Sensor } from '@site/model/sensor';
import { SensorService } from '@site/service/sensor.service';
import { SensorComponent } from './sensor.component';
import { Classification } from '@site/model/classification';

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
    wavelengths: Classification[] = []
    bsModalRef: BsModalRef;
    message: string = null;

    constructor(
        private service: SensorService,
        private modalService: BsModalService
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
		this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = "Delete";

        this.bsModalRef.content.onConfirm.subscribe( data => {
            this.remove( sensor );
        } );
    }

    edit( sensor: Sensor ): void {
        this.service.get( sensor.oid ).then( res => {
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
