///
///
///

import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, Input, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ErrorHandler } from '@shared/component';

import { ManagementService } from '@site/service/management.service';
import { SiteEntity, ODMRunConfig } from '@site/model/management';
import { Subject } from 'rxjs';


@Component({
    selector: 'run-ortho-modal',
    templateUrl: './run-ortho-modal.component.html',
    styleUrls: ['./artifact-page.component.css']
})
export class RunOrthoModalComponent implements OnInit, OnDestroy {

    message: string = null;
    entity: SiteEntity = null;
    config: ODMRunConfig = {
        processPtcloud: false,
        processDem: false,
        processOrtho: false,
        includeGeoLocationFile: false,
        includeGroundControlPointFile: false,
        outFileNamePrefix: '',
        resolution: 5,
        videoResolution: 4000,
        matcherNeighbors: 0,
        minNumFeatures: 10000,
        pcQuality: "MEDIUM",
        featureQuality: "HIGH",
        radiometricCalibration: "NONE",
        geoLocationFormat: "RX1R2",
        productName: null
    };

    isAdvancedSettingsCollapsed = true;

    /*
     * Called on confirm
     */
    public onConfirm: Subject<any>;

    constructor(public bsModalRef: BsModalRef, private service: ManagementService,) { }

    init(entity: SiteEntity) {
        this.entity = entity;
        this.config.radiometricCalibration = this.entity.sensor.sensorType.isMultispectral ? "CAMERA" : "NONE";
        
        this.service.getDefaultODMRunConfig(this.entity.id).then((config: ODMRunConfig) => {
			this.config = config;
            this.config.productName = null;

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
    }

    ngOnInit(): void {
        this.onConfirm = new Subject();
    }

    isValid(): boolean {
        return this.config.processPtcloud || this.config.processDem || this.config.processOrtho;
    }


    ngOnDestroy(): void {
        this.onConfirm.unsubscribe();
    }

    confirm(): void {
        this.onConfirm.next(this.config);
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
