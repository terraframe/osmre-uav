///
///
///

import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { ErrorHandler } from '@shared/component';
import { ManagementService } from '@site/service/management.service';

import { Page, Selection } from '@site/model/management';

@Component({
	standalone: false,
  selector: 'metadata-modal',
	templateUrl: './metadata-modal.component.html',
	styleUrls: []
})
export class MetadataModalComponent implements OnInit, OnDestroy {

	collectionId?: string | null = null;

	productId?: string | null = null;

	message: string = null;

	disabled: boolean = false;

	isOldFormat: boolean = false;

	page: Page = null;

	/*
	 * Observable subject called when metadata upload is successful
	 */
	public onMetadataChange: Subject<Selection>;


	constructor(public bsModalRef: BsModalRef, private service: ManagementService) { }

	ngOnInit(): void {
		this.onMetadataChange = new Subject();
	}

	ngOnDestroy(): void {
		this.onMetadataChange.unsubscribe();
	}

	initCollection(collectionId: string, collectionName: string): void {
		this.collectionId = collectionId;

		this.service.getMetadataOptions({ collectionId : this.collectionId }).then((options) => {
			this.loadOptions(options, collectionName);
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	initStandaloneProduct(productId: string, label: string): void {
		this.productId = productId;

		this.service.getMetadataOptions({ productId: this.productId }).then((options) => {
			this.loadOptions(options, label);
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	loadOptions(options, label) {
		this.isOldFormat = (options.uav == null || options.sensor == null);

		this.page = {
			selection: {
				type: 'CATEGORY',
				isNew: false,
				label: label,
				exifIncluded : options.exifIncluded,
				northBound : options.northBound,
				southBound : options.southBound,
				eastBound : options.eastBound,
				westBound : options.westBound,
				acquisitionDateStart : options.acquisitionDateStart,
				acquisitionDateEnd : options.acquisitionDateEnd,	
				uav: options.uav != null ? options.uav.oid : null,
				sensor: options.sensor != null ? options.sensor.oid : null,
				pointOfContact: {
					name: options.name,
					email: options.email
				},
				flyingHeight : options.flyingHeight,
				numberOfFlights : options.numberOfFlights,
				percentEndLap : options.percentEndLap,
				percentSideLap : options.percentSideLap,
				areaCovered : options.areaCovered,
				weatherConditions : options.weatherConditions,
				artifacts: options.artifacts
			}
		}

		if (this.collectionId != null) {
			this.page.selection.collectionId = this.collectionId;
		} else if (this.productId != null) {
			this.page.selection.productId = this.productId;
		}
	}

	close(): void {
		this.bsModalRef.hide();
	}


	handleSubmit(): void {

		this.service.applyMetadata(this.page.selection).then(() => {
			this.onMetadataChange.next(this.page.selection);
			this.bsModalRef.hide();
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}
}
