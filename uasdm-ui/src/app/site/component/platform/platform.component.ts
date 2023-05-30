///
///
///

import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { ErrorHandler } from '@shared/component';

import { Platform } from '@site/model/platform';
import { PlatformService } from '@site/service/platform.service';
import { Classification } from '@site/model/classification';
import { ClassificationService, Endpoint } from '@site/service/classification.service';
import { SensorService } from '@site/service/sensor.service';
import { Sensor } from '@site/model/sensor';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '@shared/service/auth.service';

@Component({
    selector: 'platform',
    templateUrl: './platform.component.html',
    styleUrls: []
})
export class PlatformComponent implements OnInit {
    
    isAdmin:boolean = false;
    
    original: Platform;
    platform: Platform;
    newInstance: boolean = false;

    message: string = null;

    manufacturers: Classification[] = [];
    types: Classification[] = [];
    sensors: { oid: string, name: string }[] = [];
    mode: string = 'READ';

    constructor(private service: PlatformService, private sensorService: SensorService,
        private classificationService: ClassificationService, private authService: AuthService,
        private route: ActivatedRoute, private router: Router) { 
            
        this.isAdmin = this.authService.isAdmin(); 
    }

    ngOnInit(): void {
        const oid = this.route.snapshot.params['oid'];

        if (oid === '__NEW__') {
            this.service.newInstance().then((platform: Platform) => {
                this.platform = platform;
                this.newInstance = true;
                this.mode = 'WRITE';
            });
        }
        else {
            this.service.get(oid).then((platform: Platform) => {
                this.platform = platform;
                this.original = JSON.parse(JSON.stringify(this.platform));
            });
        }

        this.classificationService.getAll(Endpoint.PLATFORM_MANUFACTURER).then(manufacturers => {
            this.manufacturers = manufacturers;
        });

        this.classificationService.getAll(Endpoint.PLATFORM_TYPE).then(types => {
            this.types = types;
        });

        this.sensorService.getAll().then(sensors => {
            this.sensors = sensors;
        });
    }

    handleOnSubmit(): void {
        this.message = null;

        this.service.apply(this.platform).then(data => {
            // this.platform = data;
            // this.mode = 'READ';

            // if (this.newInstance) {
            //     this.router.navigate(['/site/platform', data.oid]);
            //     this.newInstance = false;
            //     this.original = data;
            // }
            this.router.navigate(['/site/equipment']);

        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleOnCancel(): void {
        this.message = null;

        this.platform = JSON.parse(JSON.stringify(this.original));
        this.mode = 'READ';
    }

    handleOnEdit(): void {
        this.mode = 'WRITE';
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

    getSensorName(oid: string): string {
        const index = this.sensors.findIndex(s => s.oid === oid);

        if (index !== -1) {
            return this.sensors[index].name;
        }

        return '';
    }



    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
