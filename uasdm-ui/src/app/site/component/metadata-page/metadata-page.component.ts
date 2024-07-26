///
///
///

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { ManagementService } from '@site/service/management.service';
import { Page } from '../modal/upload-modal.component';
import { MetadataOptions } from '@site/model/uav';
import { Observable, Observer } from 'rxjs';
import { UAVService } from '@site/service/uav.service';
import { fadeInOnEnterAnimation, fadeOutOnLeaveAnimation } from 'angular-animations';

@Component({
	selector: 'metadata-page',
	templateUrl: './metadata-page.component.html',
	styleUrls: [],
	animations: [
		fadeInOnEnterAnimation(),
		fadeOutOnLeaveAnimation()
	]
})
export class MetadataPageComponent implements OnInit {
	/*
	 * page being updated
	 */
	@Input() page: Page;

	@Output() pageChange = new EventEmitter<Page>();

	/* 
	 * Datasource to get search responses
	 */
	dataSource: Observable<any>;
	search: string = '';
	field: string = 'serialNumber';

	uav: MetadataOptions = null;

	metaObject: any = null;

	constructor(private service: ManagementService, private uavService: UAVService) {
		this.dataSource = new Observable((observer: Observer<object>) => {
			this.uavService.search(this.search, this.field).then(results => {
				observer.next(results);
			})
		});
	}

	ngOnInit(): void {

		if (this.page.selection.pointOfContact == null) {
			this.service.getMetadataOptions(null).then(metadataOption => {
				this.page.selection.pointOfContact = {
					name: metadataOption.name,
					email: metadataOption.email
				};



			});
		}

		if (this.page.selection.uav != null) {
			this.getUavInformation(this.page.selection.uav, false);
		}

		if (this.page.selection.sensor != null) {
			this.onSensorChange();
		}
	}

	handlePageChange(): void {
		this.pageChange.emit(this.page);
	}

	onSensorChange(): void {

		this.metaObject = null;

		this.service.getUAVMetadata(this.page.selection.uav, this.page.selection.sensor).then((options) => {

			this.metaObject = {
				uav: options.uav,
				sensor: options.sensor
			};

		}).catch((err: HttpErrorResponse) => {
			// this.error(err);
		});

		this.handlePageChange();
	}

	handleUavClick(event: any): void {
		this.getUavInformation(event.item.oid, true);

		this.pageChange.emit(this.page);
	}

	getUavInformation(oid: string, reset: boolean): void {
		this.uav = null;
		this.metaObject = null;

		if (reset) {
			this.page.selection.uav = null;
			this.page.selection.sensor = null;
		}

		if (oid != null) {
			this.page.selection.uav = oid;

			this.uavService.getMetadataOptions(oid).then(metadataOption => {
				this.uav = metadataOption;

				if (!reset) {
					this.search = this.uav.serialNumber;
				}
			});
		}
	}

}
