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

import { RuntimeEstimate, getRuntimeDisplay } from '@site/model/odmrun';
import { NgIf, NgTemplateOutlet } from '@angular/common';
import { BooleanFieldComponent } from '../../../shared/component/boolean-field/boolean-field.component';
import { FormsModule } from '@angular/forms';
import { CollapseDirective } from 'ngx-bootstrap/collapse';
import { ForbiddenNameDirective } from '../../directive/forbidden-name.directive';

@Component({
    standalone: true,
    selector: 'run-process-modal',
    templateUrl: './run-process-modal.component.html',
    styleUrls: ['./artifact-page.component.css', './run-process-modal.component.css'],
    imports: [NgIf, BooleanFieldComponent, FormsModule, CollapseDirective, ForbiddenNameDirective, NgTemplateOutlet]
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

    estimate: RuntimeEstimate;

    public loadingEstimate:boolean = false;

    /*
     * Called on confirm
     */
    public onConfirm: Subject<ProcessConfig>;

    public getRuntimeDisplay = getRuntimeDisplay;

    // Make the process config type usable in the HTML template
    readonly ProcessConfigType = ProcessConfigType;

    constructor(public bsModalRef: BsModalRef, private service: ManagementService,) { }

    init(entity: SiteEntity) {
        this.entity = entity;
        this.config.radiometricCalibration = (this.entity.isRadiometric || this.entity.isMultispectral) ? "CAMERA" : "NONE";

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

    configChange(): void {
        this.loadingEstimate = true;
        this.service.estimateRuntime(this.entity.id, this.config).then((estimate: RuntimeEstimate) => {
            this.estimate = estimate;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        }).finally(() => {
            this.loadingEstimate = false;
        });
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

    isVideo(strict: boolean = false): boolean {
        if (strict)
            return this.entity.format.toLowerCase().includes('video_');
        else
            return !(this.entity && this.entity.format) || this.entity.format.toLowerCase().includes('video_');
    }

    isRadiometric(): boolean {
        return !(this.entity && this.entity.format) || this.entity.format.toLowerCase().includes('radiometric');
    }

    isMultispectral(): boolean {
        return !(this.entity && this.entity.format) || this.entity.format.toLowerCase().includes('multispectral');
    }

    

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
