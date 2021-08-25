import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { ErrorHandler } from '@shared/component';

import { Sensor } from '@site/model/sensor';
import { SensorService } from '@site/service/sensor.service';
import { ClassificationService, Endpoint } from '@site/service/classification.service';
import { Classification } from '@site/model/classification';
import { ActivatedRoute, Router } from '@angular/router';


@Component({
	selector: 'sensor',
	templateUrl: './sensor.component.html',
	styleUrls: []
})
export class SensorComponent implements OnInit {
    original: Sensor;
	sensor: Sensor;
	newInstance: boolean = false;

	message: string = null;

	wavelengths: Classification[] = [];
	types: Classification[] = [];
    mode: string = 'READ';

	constructor(private service: SensorService, private classificationService: ClassificationService, private route: ActivatedRoute, private router: Router
		) { }

	ngOnInit(): void {
        const oid = this.route.snapshot.params['oid'];

        if (oid === '__NEW__') {
            this.service.newInstance().then((sensor: Sensor) => {
                this.sensor = sensor;
                this.newInstance = true;
                this.mode = 'WRITE';
            });
        }
        else {
            this.service.get(oid).then((sensor: Sensor) => {
                this.sensor = sensor;
                this.original = JSON.parse(JSON.stringify(this.sensor));
            });
        }

		this.classificationService.getAll(Endpoint.WAVE_LENGTH).then(wavelengths => {
			this.wavelengths = wavelengths;
		});

		this.classificationService.getAll(Endpoint.SENSOR_TYPE).then(types => {
			this.types = types;
		});
	}

	handleOnSubmit(): void {
		this.message = null;

		this.service.apply(this.sensor).then(data => {
            this.sensor = data;
            this.mode = 'READ';

            if (this.newInstance) {
                this.router.navigate(['/site/sensor', data.oid]);
				this.newInstance = false;
                this.original = data;
            }
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

    handleOnCancel(): void {
        this.message = null;

        this.sensor = JSON.parse(JSON.stringify(this.original));    
        this.mode = 'READ';
    }

    handleOnEdit(): void {
        this.mode = 'WRITE';
    }

	updateSelectedWaveLength(wavelength: Classification, checked: boolean): void {

		const indexOf = this.sensor.wavelengths.findIndex(w => wavelength.oid === w);

		if (checked) {

			if (indexOf < 0) {
				this.sensor.wavelengths.push(wavelength.oid);

			}
		} else {
			if (indexOf > -1) {
				this.sensor.wavelengths.splice(indexOf, 1);
			}
		}
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}

}
