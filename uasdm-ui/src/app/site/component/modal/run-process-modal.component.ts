///
///
///

import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, Input, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ErrorHandler } from '@shared/component';

import { ManagementService } from '@site/service/management.service';
import { SiteEntity, ProcessConfig, ProcessConfigType } from '@site/model/management';
import { Subject } from 'rxjs';


@Component({
    standalone: false,
  selector: 'run-process-modal',
    templateUrl: './run-process-modal.component.html',
    styleUrls: ['./artifact-page.component.css']
})
export class RunProcessModalComponent implements OnInit, OnDestroy {

    message: string = null;
    entity: SiteEntity = null;
    config: ProcessConfig = {
        type: ProcessConfigType.ODM,
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
        productName: null,
        generateCopc: true
    };

    isAdvancedSettingsCollapsed = true;

    /*
     * Called on confirm
     */
    public onConfirm: Subject<ProcessConfig>;

    // Make the process config type usable in the HTML template
    readonly ProcessConfigType = ProcessConfigType;

    constructor(public bsModalRef: BsModalRef, private service: ManagementService,) { }

    init(entity: SiteEntity) {
        this.entity = entity;
        this.config.radiometricCalibration = this.entity.sensor.sensorType.isMultispectral ? "CAMERA" : "NONE";

        this.service.getDefaultRunConfig(this.entity.id).then((config: ProcessConfig) => {
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
        if (this.config.type === ProcessConfigType.LIDAR) {
            return (this.config.generateCopc
                || this.config.generateGSM
                || this.config.generateTerrainModel
                || this.config.generateTreeCanopyCover
                || this.config.generateTreeStructure);
        }

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
