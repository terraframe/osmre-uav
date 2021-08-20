import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { ErrorHandler } from '@shared/component';

import { Platform } from '@site/model/platform';
import { PlatformService } from '@site/service/platform.service';
import { Classification } from '@site/model/classification';
import { PlatformManufacturerService, PlatformTypeService } from '@site/service/classification.service';
import { SensorService } from '@site/service/sensor.service';
import { Sensor } from '@site/model/sensor';


@Component({
    selector: 'platform',
    templateUrl: './platform.component.html',
    styleUrls: []
})
export class PlatformComponent implements OnInit {
    platform: Platform;
    newInstance: boolean = false;

    message: string = null;

    manufacturers: Classification[] = [];
    types: Classification[] = [];
    sensors: Sensor[] = [];

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onPlatformChange: Subject<Platform>;

    constructor(private service: PlatformService, private sensorService: SensorService,
        private typeService: PlatformTypeService, private manufacturerService: PlatformManufacturerService, public bsModalRef: BsModalRef) { }

    ngOnInit(): void {
        this.onPlatformChange = new Subject();

		this.manufacturerService.getAll().then(manufacturers => {
			this.manufacturers = manufacturers;
		});

		this.typeService.getAll().then(types => {
			this.types = types;
		});

		this.sensorService.getAll().then(sensors => {
			this.sensors = sensors;
		});

    }

    handleOnSubmit(): void {
        this.message = null;

        this.service.apply(this.platform).then(data => {
            this.onPlatformChange.next(data);
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleOnCancel(): void {
        this.message = null;
        this.bsModalRef.hide();
    }

    updateSelectedSensor(sensor: Classification, checked: boolean): void {

        const indexOf = this.platform.sensors.findIndex(w => sensor.oid === w);

        if (checked) {

            if (indexOf < 0) {
                this.platform.sensors.push(sensor.oid);

            }
        } else {
            if (indexOf > -1) {
                this.platform.sensors.splice(indexOf, 1);
            }
        }
    }


    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
