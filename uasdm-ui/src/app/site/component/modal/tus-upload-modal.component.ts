///
///
///

import { Component, OnInit, KeyValueDiffers, HostListener, OnDestroy } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';


import { Subject } from 'rxjs';


import { BasicConfirmModalComponent, ErrorHandler } from '@shared/component';

import { SiteEntity, UploadTask } from '@site/model/management';
import { MetadataService } from '@site/service/metadata.service';

import {
	fadeInOnEnterAnimation,
	fadeOutOnLeaveAnimation
} from 'angular-animations';
import EnvironmentUtil from '@core/utility/environment-util';
import { UploadService } from '@site/service/upload.service';
import { UploadProgress } from '@site/model/upload';
import { ModalTypes } from '@shared/model/modal';
import { ManagementService } from '@site/service/management.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
	selector: 'tus-upload-modal',
	templateUrl: './tus-upload-modal.component.html',
	styleUrls: [
		'./upload-modal.component.css',
	],
	animations: [
		fadeInOnEnterAnimation(),
		fadeOutOnLeaveAnimation()
	]
})
export class TusUploadModalComponent implements OnInit, OnDestroy {

	/*
	 * List of hierarchies
	 */
	existingTask: UploadTask | null;

	component: SiteEntity = null;

	uploadTarget: string = null;

	selectedFile: File | null = null

	progress: UploadProgress | null = null

	isUploading: boolean = false
	isSuccessful: boolean = false
	invalidFile: boolean = false;

	message: string | null = null;

	extensions: string = ".zip,.tar.gz"


	public onUploadComplete: Subject<void>;
	public onUploadCancel: Subject<void>;

	constructor(
		private service: ManagementService,
		private modalService: BsModalService,
		private uploadService: UploadService,
		public bsModalRef: BsModalRef) {
	}


	ngOnInit(): void {
		this.onUploadComplete = new Subject();
		this.onUploadCancel = new Subject();

		// this.service.getMetadataOptions(null).then((options) => {
		// 	this.sensors = options.sensors;
		// 	this.platforms = options.platforms;
		// }).catch((err: HttpErrorResponse) => {
		// 	this.error(err);
		// });

		this.uploadService.findAllUploads().then(uploads => {
			uploads.forEach(upload => console.log('Found upload', upload));
		})
	}

	ngOnDestroy(): void {
		this.onUploadComplete.unsubscribe();
		this.onUploadCancel.unsubscribe();
	}

	ngAfterViewInit() {

	}

	ngDoCheck() {

		// if (this.uploader) {
		// 	const change = this.differ.diff(this.uploader);
		// 	if (change) {
		// 		this.setExistingTask();
		// 	}
		// }
	}

	init(component: SiteEntity, uploadTarget: string, existingTask?: UploadTask, productName?: string): void {

		this.component = component;
		this.uploadTarget = uploadTarget;
		this.existingTask = existingTask;

		if (this.existingTask != null) {
			// this.extensions = this.existingTask.filename;
		}

		// this.processUpload = this.uploadTarget === 'raw';

	}

	close(): void {
		this.bsModalRef.hide();
	}

	onFileSelected(event: Event): void {
		const input = event.target as HTMLInputElement
		if (input.files && input.files.length > 0) {

			if (this.existingTask == null || this.existingTask.filename === input.files[0].name) {
				this.selectedFile = input.files[0];
				this.progress = null;
			}
			else {
				this.invalidFile = true;
			}
		}
	}

	isPageValid(): boolean {
		// if (this.uploader != null) {
		// 	const uploads: any = this.uploader.getUploads();

		// 	return (uploads != null && uploads.length > 0);
		// };

		return true;
	}

	handleUpload(): void {

		if (!this.selectedFile) return

		this.isUploading = true;
		this.isSuccessful = false;

		const uploadEndpoint = EnvironmentUtil.getApiUrl() + "/api/tus-upload"

		this.uploadService.startUpload(
			this.selectedFile,
			uploadEndpoint,
			this.component.id,
			this.uploadTarget,
			(progress) => {
				this.progress = progress
			},
			() => {
				this.isUploading = false;
				this.isSuccessful = true;
				this.progress = null;
				this.existingTask = null;

				this.onUploadComplete.next();
			},
			(error: Error) => {
				this.error(error);
				this.isUploading = false
			},
		)
	}

	removeUpload(): void {
		const modal = this.modalService.show(BasicConfirmModalComponent, {
			animated: false,
			backdrop: true, class: 'modal-xl',
			ignoreBackdropClick: true,
		});
		modal.content.message = 'Are you sure you want to cancel the upload of [' + this.existingTask.filename + ']';
		modal.content.type = ModalTypes.danger;
		modal.content.submitText = 'Cancel Upload';

		modal.content.onConfirm.subscribe(data => {
			this.service.removeUpload(this.existingTask.resumable.uploadUrl)
				.then(() => {
					this.uploadService.clearUpload(this.existingTask.resumable.urlStorageKey);

					this.existingTask = null;

					this.onUploadCancel.next();
					this.bsModalRef.hide();
				}).catch((err: HttpErrorResponse) => {
					this.error(err);
				});
		});
	}

	public canDeactivate(): boolean {
		return this.isUploading;
	}

	@HostListener('window:beforeunload', ['$event'])
	unloadNotification($event: any) {
		if (this.isUploading) {
			$event.returnValue = 'An upload is currently in progress. Are you sure you want to leave?';
		}
	}

	error(err: any): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}


}
