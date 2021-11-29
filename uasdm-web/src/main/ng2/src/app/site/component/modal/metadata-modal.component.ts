import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { ErrorHandler } from '@shared/component';
import { ManagementService } from '@site/service/management.service';

import { WAVELENGTHS } from '@site/model/sensor';

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
		pointOfContact: {
			name: "",
			email: ""
		},
		platform: {
			otherName: "",
			class: "",
			type: "",
			serialNumber: "",
			faaIdNumber: ""
		},
		sensor: {
			otherName: "",
			type: "",
			model: "",
			wavelength: [],
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

	wavelengths: string[] = WAVELENGTHS;

	constructor(public bsModalRef: BsModalRef, private service: ManagementService) { }

	init(collectionId: string): void {
		this.collectionId = collectionId;

		this.onMetadataChange = new Subject();

		this.service.getMetadataOptions(null).then((options) => {

			this.metaObject.pointOfContact.name = options.name;
			this.metaObject.pointOfContact.email = options.email;

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}


	handleSubmit(): void {

		// this.metaObject.imageWidth = this.imageWidth;
		// this.metaObject.imageHeight = this.imageHeight;

		this.service.submitCollectionMetadata(this.collectionId, this.metaObject).then(() => {
			this.bsModalRef.hide();
			this.onMetadataChange.next(this.collectionId);
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}
	
	updateSelectedWaveLength(wavelength: string, checked: boolean): void {

		const indexOf = this.metaObject.sensor.wavelength.indexOf(wavelength)

		if (checked) {

			if (indexOf < 0) {
				this.metaObject.sensor.wavelength.push(wavelength);

			}
		} else {
			if (indexOf > -1) {
				this.metaObject.sensor.wavelength.splice(indexOf, 1);
			}
		}
	}	

	error(err: HttpErrorResponse): void {
	  this.message = ErrorHandler.getMessageFromError(err);
	}
}
