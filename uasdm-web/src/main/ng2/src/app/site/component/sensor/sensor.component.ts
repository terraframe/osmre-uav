import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { ErrorHandler } from '@shared/component';

import { Sensor } from '@site/model/sensor';
import { SensorService } from '@site/service/sensor.service';
import { SensorTypeService, WaveLengthService } from '@site/service/classification.service';
import { Classification } from '@site/model/classification';


@Component({
	selector: 'sensor',
	templateUrl: './sensor.component.html',
	styleUrls: []
})
export class SensorComponent implements OnInit {
	sensor: Sensor;
	newInstance: boolean = false;

	message: string = null;

	wavelengths: Classification[] = [];
	types: Classification[] = [];

	/*
	 * Observable subject for TreeNode changes.  Called when create is successful 
	 */
	public onSensorChange: Subject<Sensor>;

	constructor(private service: SensorService, private wavelengthService: WaveLengthService,
		private typeService: SensorTypeService, public bsModalRef: BsModalRef) { }

	ngOnInit(): void {
		this.onSensorChange = new Subject();

		this.wavelengthService.getAll().then(wavelengths => {
			this.wavelengths = wavelengths;
		});

		this.typeService.getAll().then(types => {
			this.types = types;
		});
	}

	handleOnSubmit(): void {
		this.message = null;

		this.service.apply(this.sensor).then(data => {
			this.onSensorChange.next(data);
			this.bsModalRef.hide();
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	handleOnCancel(): void {
		this.message = null;

		this.bsModalRef.hide();
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
