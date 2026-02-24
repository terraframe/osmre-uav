///
///
///

import { Component, OnInit, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';

import { CollectionArtifacts, ProductDetail, SiteEntity } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';

import { BasicConfirmModalComponent } from '@shared/component';
import EnvironmentUtil from '@core/utility/environment-util';
import { environment } from 'src/environments/environment';
import { ODMRunModalComponent } from './odmrun-modal.component';
import { ModalTypes } from '@shared/model/modal';
import { TusUploadModalComponent } from './tus-upload-modal.component';
import { NgIf, NgFor, NgStyle, NgClass } from '@angular/common';
import { BsDropdownDirective, BsDropdownToggleDirective, BsDropdownMenuDirective } from 'ngx-bootstrap/dropdown';
import { ArtifactUploadItemComponent } from '../artifact-upload-item/artifact-upload-item.component';
import { BooleanFieldComponent } from '@shared/component/boolean-field/boolean-field.component';
import { WebsocketService } from '@shared/service/websocket.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
	standalone: true,
	selector: 'artifact-page',
	templateUrl: './artifact-page.component.html',
	styleUrls: ['./artifact-page.component.css'],
	providers: [],
	imports: [NgIf, NgFor, NgStyle, BsDropdownDirective, BsDropdownToggleDirective, BsDropdownMenuDirective, NgClass, ArtifactUploadItemComponent, BooleanFieldComponent]
})
export class ArtifactPageComponent implements OnInit, OnDestroy {

	@Input() entity: SiteEntity;
	@Input() standaloneProduct?: ProductDetail;
	@Input() processRunning: boolean;
	@Input() edit: boolean = false;

	@Input() config = {
		processPtcloud: false,
		processDem: false,
		processOrtho: false
	};

	@Output() onError = new EventEmitter<HttpErrorResponse>();

	loading = false;

	groups: CollectionArtifacts[];

	sections = [{
		label: 'Ptcloud',
		folder: 'ptcloud'
	}, {
		label: 'DEM',
		folder: 'dem'
	}, {
		label: 'Ortho',
		folder: 'ortho'
	}];

	context: string;

	constructor(
		private service: ManagementService,
		private modalService: BsModalService,
		private websocketService: WebsocketService
	) {
		this.context = EnvironmentUtil.getApiUrl();

		this.websocketService.getNotifier()
			.pipe(takeUntilDestroyed())
			.subscribe((message) => {
				if (message.type === 'UPLOAD_JOB_CHANGE'
					&& message.content.status === 'Complete'
					&& message.content.collection === this.entity.id) {
					this.loadArtifacts();
				}

				if (message.type === 'PRODUCT_GROUP_CHANGE'
					&& message.content.collection === this.entity.id) {
					this.loadArtifacts();
				}

			});

	}

	ngOnInit(): void {

		this.loadArtifacts();
	}

	ngOnDestroy(): void {
	}

	loadArtifacts(): void {
		this.loading = true;
		this.service.getArtifacts(this.entity.id).then(groups => {

			if (this.standaloneProduct != null) {
				groups = groups.filter(g => g.productName === this.standaloneProduct.productName)
			}

			this.loading = false;

			this.groups = groups;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	showODMRun(artifact): void {
		const modal = this.modalService.show(ODMRunModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: false,
			'class': 'modal-xl'
		});

		modal.content.initOnArtifact(artifact);
	}

	handleDownloadFile(item: SiteEntity): void {
		window.location.href = environment.apiUrl + '/api/project/download?id=' + this.entity.id + "&key=" + item.key;
	}


	handleUpload(productName: string, folderName: string): void {

		const modal = this.modalService.show(TusUploadModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'upload-modal modal-xl'
		});
		modal.content.init(this.entity, folderName, null, productName);

		// modal.content.onUploadComplete.subscribe(oid => {

		//   this.handleViewSite(oid);
		// });
	}

	handleRemove(productName: string, section: { label: string, folder: string }): void {

		const modal = this.modalService.show(BasicConfirmModalComponent, {
			animated: true,
			backdrop: true, class: 'modal-xl',
			ignoreBackdropClick: true,
		});
		modal.content.message = 'Do you want to delete the [' + section.label + '] products? This action cannot be undone.';
		modal.content.type = ModalTypes.danger;
		modal.content.submitText = 'Delete';

		modal.content.onConfirm.subscribe(() => {
			this.service.removeArtifacts(this.entity.id, productName, section.folder).then(artifacts => {

				// TODO: Handle refresh
				//				this.artifacts = artifacts;
			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		});
	}

	handleRemoveGroup(productName: string): void {

		const modal = this.modalService.show(BasicConfirmModalComponent, {
			animated: true,
			backdrop: true,
			class: 'modal-xl',
			ignoreBackdropClick: true,
		});
		modal.content.message = 'Do you want to delete the product group [' + productName + ']? This action cannot be undone.';
		modal.content.type = ModalTypes.danger;
		modal.content.submitText = 'Delete';

		modal.content.onConfirm.subscribe(() => {
			this.service.removeProduct(this.entity.id, productName).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		});
	}

	handleSetPrimary(productName: string): void {

		this.service.setPrimaryProduct(this.entity.id, productName).then(() => {
			this.entity.hasAllZip = false;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}


	handleDownloadReport(productName: string, section: { label: string, folder: string }): void {

		let url = environment.apiUrl + '/api/project/download-report?'
		url += 'colId=' + encodeURIComponent(this.entity.id);
		url += "&productName=" + encodeURIComponent(productName)
		url += "&folder=" + encodeURIComponent(section.folder);

		window.location.href = url;
	}

	capitalize(str): string {
		return str.replace(/^\w/, c => c.toUpperCase());
	}

	error(err: HttpErrorResponse): void {
		this.onError.emit(err);
	}


	/**
	 * Format bytes as human-readable text.
	 * Credit: https://stackoverflow.com/questions/10420352/converting-file-size-in-bytes-to-human-readable-string
	 * 
	 * @param bytes Number of bytes.
	 * @param si True to use metric (SI) units, aka powers of 1000. False to use 
	 *           binary (IEC), aka powers of 1024.
	 * @param dp Number of decimal places to display.
	 * 
	 * @return Formatted string.
	 */
	byteCountAsHumanReadable(bytes, si = false, dp = 1) {
		if (bytes == -1) { return ""; }

		const thresh = si ? 1000 : 1024;

		if (Math.abs(bytes) < thresh) {
			return bytes + ' B';
		}

		const units = si
			? ['kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
			: ['KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
		let u = -1;
		const r = 10 ** dp;

		do {
			bytes /= thresh;
			++u;
		} while (Math.round(Math.abs(bytes) * r) / r >= thresh && u < units.length - 1);


		return bytes.toFixed(dp) + ' ' + units[u];
	}

}
