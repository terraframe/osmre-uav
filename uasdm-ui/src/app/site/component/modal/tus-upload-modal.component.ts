///
///
///

import { Component, OnInit, ViewChild, ElementRef, KeyValueDiffers, HostListener, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

import { interval, Subject } from 'rxjs';
import { switchMap, map } from 'rxjs/operators';

//use Fine Uploader UI for traditional endpoints
import { FineUploader, UIOptions } from 'fine-uploader';

import { ErrorHandler, BasicConfirmModalComponent } from '@shared/component';

import { Sensor } from '@site/model/sensor';
import { Platform } from '@site/model/platform';
import { SiteEntity, UploadForm, Task, Selection, CollectionArtifacts, ProcessConfig, ProcessConfigType } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';
import { MetadataService } from '@site/service/metadata.service';
import { MetadataModalComponent } from './metadata-modal.component';

import { StepConfig } from '@shared/modal/step-indicator/modal-step-indicator'

import {
	fadeInOnEnterAnimation,
	fadeOutOnLeaveAnimation
} from 'angular-animations';
import EnvironmentUtil from '@core/utility/environment-util';
import { NgModel } from '@angular/forms';
import { ModalTypes } from '@shared/model/modal';
import { UploadService } from '@site/service/upload.service';
import { UploadProgress } from '@site/model/upload';

@Component({
	selector: 'tus-upload-modal',
	templateUrl: './tus-upload-modal.component.html',
	styleUrls: ['./upload-modal.component.css'],
	animations: [
		fadeInOnEnterAnimation(),
		fadeOutOnLeaveAnimation()
	]
})
export class TusUploadModalComponent implements OnInit, OnDestroy {
	// Make the process config type usable in the HTML template
	readonly ProcessConfigType = ProcessConfigType;

	disabled: boolean = false;


	taskStatusMessages: string[] = [];
	currentTask: Task = null;
	finishedTask: Task = null;
	existingTask: boolean = false;
	taskPolling: any;
	pollingIsSet: boolean = false;
	uploadVisible: boolean = true;
	selectedContinue: boolean = false;
	uploadCounter: string = "00:00:00";
	uplodeCounterInterfal: any;
	showFileSelectPanel: boolean = false;
	taskFinishedNotifications: any[] = [];

	/*
	 * List of hierarchies
	 */
	hierarchy: string[] = [];

	component: SiteEntity = null;

	uploadTarget: string = null;

	selectedFile: File | null = null

	progress: UploadProgress | null = null

	isUploading: boolean = false

	message: string | null = null;

	public onUploadComplete: Subject<void>;
	public onUploadCancel: Subject<void>;

	constructor(private metadataService: MetadataService,
		private uploadService: UploadService,
		public bsModalRef: BsModalRef,
		differs: KeyValueDiffers) {
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

	init(component: SiteEntity, uploadTarget: string, productName?: string): void {

		this.component = component;
		this.uploadTarget = uploadTarget;

		// this.processUpload = this.uploadTarget === 'raw';

		this.hierarchy = this.metadataService.getHierarchy();
		// this.selections = [];

		// Handle the case where there is an existing file upload
		if (this.existingTask) {
		}
	}

	close(): void {
		this.bsModalRef.hide();
	}

	onFileSelected(event: Event): void {
		const input = event.target as HTMLInputElement
		if (input.files && input.files.length > 0) {
			this.selectedFile = input.files[0];
			this.progress = null;
		}
	}

	closeTaskFinishedNotification(id: string): void {
		// iterate in reverse to allow splice while avoiding the reindex
		// from affecting any of the next items in the array.
		let i = this.taskFinishedNotifications.length;
		while (i--) {
			let note = this.taskFinishedNotifications[i];
			if (id === note.id) {
				this.taskFinishedNotifications.splice(i, 1);
			}
		}
	}


	setExistingTask(): void {
		// let resumable = this.uploader.getResumableFilesData() as any[];
		// if (resumable.length > 0) {
		// 	this.existingTask = true;
		// 	//            if ( !this.selectedContinue ) {
		// 	//                this.hideUploadPanel();
		// 	//            }
		// }
	}

	isPageValid(): boolean {
		// if (this.uploader != null) {
		// 	const uploads: any = this.uploader.getUploads();

		// 	return (uploads != null && uploads.length > 0);
		// };

		return true;
	}

	updateCurrentPageLabel(): void {
	}

	setIsNew(isNew: boolean): void {
	}

	handleNextPage(): void {
	}

	handleBackPage(): void {

	}

	handleUpload(): void {

		if (!this.selectedFile) return

		this.isUploading = true
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
				// this.message = 'Upload completed successfully!'
				// this.messageType = 'success'
				this.isUploading = false
			},
			(error: Error) => {
				this.error(error);
				this.isUploading = false
			},
		)
	}

	removeUpload(): void {
		// const modal = this.modalService.show(BasicConfirmModalComponent, {
		// 	animated: false,
		// 	backdrop: true, class: 'modal-xl',
		// 	ignoreBackdropClick: true,
		// });
		// modal.content.message = 'Are you sure you want to cancel the upload of [' + this.uploader.getResumableFilesData()[0].name + ']';
		// modal.content.type = ModalTypes.danger;
		// modal.content.submitText = 'Cancel Upload';

		// modal.content.onConfirm.subscribe(data => {
		// 	this.service.removeUploadTask(this.uploader.getResumableFilesData()[0].uuid)
		// 		.then(() => {
		// 			//that.uploader.clearStoredFiles();
		// 			//that.uploader.cancelAll()

		// 			// The above clearStoredFiles() and cancelAll() methods don't appear to work so 
		// 			// we are clearing localStorage manually.
		// 			localStorage.clear();

		// 			this.existingTask = false;

		// 			this.onUploadCancel.next();

		// 			this.bsModalRef.hide();
		// 		}).catch((err: HttpErrorResponse) => {
		// 			this.error(err);
		// 		});
		// });
	}

	countUpload(thisRef: any): void {
		let ct = 0;

		function incrementSeconds() {
			ct += 1;

			let hours = Math.floor(ct / 3600)
			let minutes = Math.floor((ct % 3600) / 60);
			let seconds = Math.floor(ct % 60);

			let hoursStr = minutes < 10 ? "0" + hours : hours;
			let minutesStr = minutes < 10 ? "0" + minutes : minutes;
			let secondsStr = seconds < 10 ? "0" + seconds : seconds;

			thisRef.uploadCounter = hoursStr + ":" + minutesStr + ":" + secondsStr;
		}

		thisRef.uplodeCounterInterfal = setInterval(incrementSeconds, 1000);
	}

	public canDeactivate(): boolean {
		return this.disabled;
	}

	@HostListener('window:beforeunload', ['$event'])
	unloadNotification($event: any) {
		if (this.disabled) {
			$event.returnValue = 'An upload is currently in progress. Are you sure you want to leave?';
		}
	}

	error(err: any): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}


}
