import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { ErrorHandler } from '@shared/component';
import { ManagementService } from '@site/service/management.service';

import { Sensor } from '@site/model/sensor';
import { Platform } from '@site/model/platform';

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

	metaObject: any = {
		collectionId: "",
		// agency:{
		//     name:"Department of Interior",
		//     shortName: "",
		//     fieldCenter: ""
		// },
		pointOfContact: {
			name: "",
			email: ""
		},
		// project: {
		//     name:"",
		//     shortName:"",
		//     description:""
		// },
		// mission: {
		//     name:"",
		//     description:""
		// },
		// collect: {
		//     name:"",
		//     description:""
		// },
		uav: {
			name: "",
			bureau: "",
			platformType: "",
			manufacturer: "",
			serialNumber: "",
			faaNumber: ""
		},
		sensor: {
			name: "",
			sensorType: "",
			wavelength: [],
			// imageWidth:"",
			// imageHeight:"",
			sensorWidth: "",
			sensorWidthUnits: "mm",
			sensorHeight: "",
			sensorHeightUnits: "mm",
			pixelSizeWidth: "",
			pixelSizeHeight: ""
		},
		upload: {
			dataType: "raw"
		}
	};

    /*
     * Observable subject called when metadata upload is successful
     */
	public onMetadataChange: Subject<string>;

	wavelengths: string[] = [];

	otherSensorId: string = "";
	otherPlatformId: string = "";

	constructor(public bsModalRef: BsModalRef, private service: ManagementService) { }

	init(collectionId: string): void {
		this.collectionId = collectionId;

		this.onMetadataChange = new Subject();

		this.service.getMetadataOptions(this.collectionId).then((options) => {

			this.metaObject.pointOfContact.name = options.name;
			this.metaObject.pointOfContact.email = options.email;
			this.metaObject.sensor = options.sensor;
			this.metaObject.uav = options.uav;

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}


	handleSubmit(): void {

		this.metaObject.collectionId = this.collectionId;
		// this.metaObject.imageWidth = this.imageWidth;
		// this.metaObject.imageHeight = this.imageHeight;

		this.service.submitCollectionMetadata(this.metaObject).then(() => {
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
