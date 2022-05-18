import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
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
export class MetadataModalComponent {
	/*
	 * collectionId for the metadata
	 */
	collectionId: string;

	message: string = null;

	disabled: boolean = false;

	// imageHeight: string;

	// imageWidth: string;

	page: Page = null;

	/*
	 * Observable subject called when metadata upload is successful
	 */
	public onMetadataChange: Subject<Selection>;


	constructor(public bsModalRef: BsModalRef, private service: ManagementService) { }

	init(collectionId: string, collectionName: string): void {
		this.collectionId = collectionId;

		this.onMetadataChange = new Subject();

		this.service.getMetadataOptions(this.collectionId).then((options) => {

			this.page = {
				selection: {
					type: 'CATEGORY',
					isNew: false,
					value: this.collectionId,
					label: collectionName,
					uav: options.uav.oid,
					sensor: options.sensor.oid,
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
			this.bsModalRef.hide();
			this.onMetadataChange.next(this.page.selection);
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}
}
