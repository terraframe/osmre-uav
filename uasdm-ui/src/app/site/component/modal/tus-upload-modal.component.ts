///
///
///

import { Component, OnInit, HostListener, OnDestroy } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import {
	fadeInOnEnterAnimation,
	fadeOutOnLeaveAnimation
} from 'angular-animations';

import { Subject } from 'rxjs';

import { BasicConfirmModalComponent, ErrorHandler } from '@shared/component';

import { ProcessConfigType, SiteEntity, Task, UploadForm, UploadTask } from '@site/model/management';

import EnvironmentUtil from '@core/utility/environment-util';
import { UploadService } from '@site/service/upload.service';
import { UploadMetadata, UploadProgress } from '@site/model/upload';
import { ModalTypes } from '@shared/model/modal';
import { ManagementService } from '@site/service/management.service';
import { HttpErrorResponse } from '@angular/common/http';
import { NgxFileDropEntry, NgxFileDropModule } from 'ngx-file-drop';
import { FormsModule } from '@angular/forms';
import { NgIf, NgFor, DecimalPipe } from '@angular/common';
import { BooleanFieldComponent } from '@shared/component/boolean-field/boolean-field.component';

type SupportedRow = { type: string; formats: string };

@Component({
    standalone: true,
    selector: 'tus-upload-modal',
    templateUrl: './tus-upload-modal.component.html',
    styleUrls: [
        './upload-modal.component.css',
    ],
    animations: [
        fadeInOnEnterAnimation(),
        fadeOutOnLeaveAnimation()
    ],
    imports: [FormsModule, NgIf, BooleanFieldComponent, NgFor, NgxFileDropModule, DecimalPipe]
})
export class TusUploadModalComponent implements OnInit, OnDestroy {

	existingTask: UploadTask | null;

	component: SiteEntity = null;

	uploadTarget: string = null;

	productName: string | null = null;

	selectedFile: File | null = null

	progress: UploadProgress | null = null

	isUploading: boolean = false
	isSuccessful: boolean = false

	validation: string = null;

	message: string | null = null;

	extensions: string = ".zip,.gz"

	metadata: UploadMetadata | null = null;

	/* 
	 * Form values
	 */

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
	}

	ngOnDestroy(): void {
		this.onUploadComplete.unsubscribe();
		this.onUploadCancel.unsubscribe();
	}

	init(component: SiteEntity, uploadTarget: string, existingTask?: UploadTask, productName?: string): void {

		this.component = component;
		this.uploadTarget = uploadTarget;
		this.existingTask = existingTask;
		this.productName = productName;

		this.metadata = {
			componentId: this.component.id,
			uploadTarget: this.uploadTarget,
			type: ProcessConfigType.ODM,
			processOrtho: true,
			processPtcloud: true,
			processDem: true
		}

		if (this.uploadTarget != null && this.uploadTarget === 'ptcloud') {
			this.extensions = ".laz,.las,.zip,.gz";
		}
		if (this.uploadTarget != null && this.uploadTarget === 'dem') {
			this.extensions = ".tif,.tiff,.zip,.gz";
		}
		if (this.uploadTarget != null && this.uploadTarget === 'ortho') {
			this.extensions = ".tif,.tiff,.png,.zip,.gz";
		}
		if (this.uploadTarget != null && this.uploadTarget === 'video') {
			this.extensions = ".mp4";
		}

	}

	close(): void {
		this.bsModalRef.hide();
	}
	
	visitTaskPage(): void {
	  // this.close won't close the outer modal... since we're modal inside a modal here
	  window.location.href = '/#/site/tasks';
	}

	onFileSelected(files: NgxFileDropEntry[]): void {
		this.selectedFile = null;
		this.validation = null;

		if (files.length > 0) {
			if (files[0].fileEntry.isFile) {
				const entry = files[0].fileEntry as FileSystemFileEntry;

				entry.file((file: File) => {
					const allowedExtensions = this.extensions.replaceAll('.', '').split(',');
					const fileExtension = file.name.split(".").pop().toLowerCase(); // 

					if (allowedExtensions.includes(fileExtension) || (this.existingTask != null && this.existingTask.filename === file.name)) {
						this.selectedFile = file;
						this.progress = null;
					}
					else {
						this.validation = "The file must be one of the following extensions: " + this.extensions;
					}
				});
			}
		}
	}

	handleUpload(): void {

		if (!this.selectedFile) return

		this.isUploading = true;
		this.isSuccessful = false;

		const uploadEndpoint = EnvironmentUtil.getApiUrl() + "/api/tus-upload"


		if (this.productName != null) {
			this.metadata.productName = this.productName;
		}

		this.uploadService.startUpload(
			this.selectedFile,
			uploadEndpoint,
			this.metadata,
			(progress) => {
				this.progress = progress
			},
			(url) => {
				this.isUploading = false;
				this.isSuccessful = true;
				this.progress = null;
				this.existingTask = null;

				this.onUploadComplete.next();
				this.selectedFile = null;
			},
			(error: Error) => {
				this.error(error);
				this.isUploading = false
			},
		)
	}

	removeUpload(): void {
		const modal = this.modalService.show(BasicConfirmModalComponent, {
			animated: true,
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

	public isVideo(): boolean {
		if (this.component.format == null) return false;

		return this.component.format.toLowerCase().includes("video");
	}

	@HostListener('window:beforeunload', ['$event'])
	unloadNotification($event: any) {
		if (this.isUploading) {
			$event.returnValue = 'An upload is currently in progress. Are you sure you want to leave?';
		}
	}

	get supportedRawFormats(): SupportedRow[] {
		if (!this.component) {
			return [];
		}

		const fmt = this.component.format;
		const rows: SupportedRow[] = [];

		// ---- Legacy behaviour (no format set) ----
		if (!fmt) {
			if (this.component.isLidar) {
				rows.push({ type: 'Pointcloud', formats: 'laz, las' });
			} else if (this.component.isRadiometric) {
				rows.push({ type: 'Radiometric Data', formats: 'tif' });
			} else {
				// default legacy RGB/multispectral behaviour
				rows.push(
				{ type: 'RGB Image Data',           formats: 'jpg, jpeg, png' },
				{ type: 'Multispectral Image Data', formats: 'tif' }
				);
			}

			// Legacy table always includes Video row
			rows.push({ type: 'Video', formats: 'mp4' });
				return rows;
		}

		// ---- New behaviour driven by CollectionFormat ----
		switch (fmt) {
			case 'LIDAR':
				rows.push({ type: 'Pointcloud', formats: 'laz, las' });
				break;

			case 'STILL_RADIOMETRIC':
				rows.push({ type: 'Radiometric Data', formats: 'tif' });
				break;

			case 'STILL_MULTISPECTRAL':
				rows.push({ type: 'Multispectral Image Data', formats: 'tif' });
				break;

			case 'STILL_IMAGERY_RGB':
			case 'STILL_THERMAL_RGB':
				rows.push({ type: 'RGB Image Data', formats: 'jpg, jpeg, png' });
				break;

			case 'VIDEO_RGB':
			case 'VIDEO_THERMAL_RGB':
			case 'VIDEO_RADIOMETRIC':
			case 'VIDEO_MULTISPECTRAL':
				rows.push({ type: 'Video', formats: 'mp4' });
				break;
		}

		return rows;
	}

	error(err: any): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}


}
