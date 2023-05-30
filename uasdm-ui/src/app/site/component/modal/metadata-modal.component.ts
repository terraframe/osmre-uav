///
///
///

import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { ErrorHandler } from '@shared/component';
import { ManagementService } from '@site/service/management.service';

import { Selection } from '@site/model/management';
import { Page } from './upload-modal.component';

@Component({
	selector: 'metadata-modal',
	templateUrl: './metadata-modal.component.html',
	styleUrls: []
})
export class MetadataModalComponent implements OnInit, OnDestroy {
	/*
	 * collectionId for the metadata
	 */
	collectionId: string;

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

	init(collectionId: string, collectionName: string): void {
		this.collectionId = collectionId;

		this.service.getMetadataOptions(this.collectionId).then((options) => {

			this.isOldFormat = (options.uav == null || options.sensor == null);

			this.page = {
				selection: {
					type: 'CATEGORY',
					isNew: false,
					value: this.collectionId,
					label: collectionName,
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
					}
				}
			}

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
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
