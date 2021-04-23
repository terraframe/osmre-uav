import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { ErrorHandler } from '@shared/component';
import { ManagementService } from '@site/service/management.service';

import { Sensor, WAVELENGTHS } from '@site/model/sensor';
import { Platform } from '@site/model/platform';


declare var acp: string;

@Component({
	selector: 'metadata-modal',
	templateUrl: './metadata-modal.component.html',
	styleUrls: []
})
export class MetadataModalComponent {
    /*
     * collectionId for the metadata
     */
	collectionId: string;

	message: string = null;

	disabled: boolean = false;

	// imageHeight: string;

	// imageWidth: string;

  // Used to facilitate OTHER selection behaviour
  frontendSelect: any = {
    platform: {
      name: "",
      otherName: ""
    },
    sensor: {
      name: "",
      otherName: ""
    }
  }

	metaObject: any = {
		// Agency:{
		//     name:"Department of Interior",
		//     shortName: "",
		//     fieldCenter: ""
		// },
		PointOfContact: {
			name: "",
			email: ""
		},
		// Project: {
		//     name:"",
		//     shortName:"",
		//     description:""
		// },
		// Mission: {
		//     name:"",
		//     description:""
		// },
		// Collect: {
		//     name:"",
		//     description:""
		// },
		Platform: {
			name: "",
			class: "",
			type: "",
			serialNumber: "",
			faaIdNumber: ""
		},
		Sensor: {
			name: "",
			type: "",
			model: "",
			wavelength: [],
			// imageWidth:"",
			// imageHeight:"",
			sensorWidth: "",
			//sensorWidthUnits: "mm",
			sensorHeight: "",
			//sensorHeightUnits: "mm",
			pixelSizeWidth: "",
			pixelSizeHeight: ""
		},
		Upload: {
			dataType: "raw"
		}
	};

    /*
     * Observable subject called when metadata upload is successful
     */
	public onMetadataChange: Subject<string>;

	sensors: Sensor[] = [];
	platforms: Platform[] = [];
	wavelengths: string[] = WAVELENGTHS;

	otherSensorId: string = "OTHER";
	otherPlatformId: string = "OTHER";

	constructor(public bsModalRef: BsModalRef, private service: ManagementService) { }

	init(collectionId: string): void {
		this.collectionId = collectionId;

		this.onMetadataChange = new Subject();

		this.service.getMetadataOptions(this.collectionId).then((options) => {
			this.sensors = options.sensors;
			this.platforms = options.platforms;

			this.metaObject.PointOfContact.name = options.name;
			this.metaObject.PointOfContact.email = options.email;
			this.metaObject.Sensor.name = options.sensor;
			this.metaObject.Platform.name = options.platform;

			this.handleSensorSelect();
			this.handlePlatformSelect();

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	handleSensorSelect(): void {
		if (this.frontendSelect.sensor.name != null && this.frontendSelect.sensor.name !== "") {
			const sensor = this.getSelectedSensor();

      this.metaObject.Sensor.name = sensor.name
			this.metaObject.Sensor.type = sensor.sensorType;
			this.metaObject.Sensor.model = sensor.model;
			this.metaObject.Sensor.wavelength = [...sensor.waveLength];
		}
		else
		{
		  this.metaObject.Sensor.name = "";
		  this.metaObject.Sensor.type = "";
		  this.metaObject.Sensor.model = "";
		  this.metaObject.Sensor.wavelength = [];
		}
	}

	handlePlatformSelect(): void {
		if (this.frontendSelect.platform.name != null && this.frontendSelect.platform.name !== "") {
			const platform = this.getSelectedPlatform();

      this.metaObject.Platform.name = platform.name;
			this.metaObject.Platform.type = platform.platformType;
		}
		else
		{
		  this.metaObject.Platform.name = "";
		  this.metaObject.Platform.type = "";
		}
	}
	
	handlePlatformOtherChange(): void {
	  this.metaObject.Platform.name = this.frontendSelect.platform.otherName;
	}
	
	handleSensorOtherChange(): void {
    this.metaObject.Sensor.name = this.frontendSelect.sensor.otherName;
  }

	getSelectedSensor(): Sensor {
		var indexOf = this.sensors.findIndex(i => i.name === this.frontendSelect.sensor.name);

		return this.sensors[indexOf];
	}

	getSelectedPlatform(): Platform {
		var indexOf = this.platforms.findIndex(i => i.name === this.frontendSelect.platform.name);

		return this.platforms[indexOf];
	}

	updateSelectedWaveLength(wavelength: string, checked: boolean): void {

		const indexOf = this.metaObject.Sensor.wavelength.indexOf(wavelength)

		if (checked) {

			if (indexOf < 0) {
				this.metaObject.Sensor.wavelength.push(wavelength);

			}
		} else {
			if (indexOf > -1) {
				this.metaObject.Sensor.wavelength.splice(indexOf, 1);
			}
		}
	}

	handleSubmit(): void {
		this.service.submitCollectionMetadata(this.collectionId, this.metaObject).then(() => {
			this.bsModalRef.hide();
			this.onMetadataChange.next(this.collectionId);
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	error(err: HttpErrorResponse): void {
	  this.message = ErrorHandler.getMessageFromError(err);
	}
}
