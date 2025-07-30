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

export class Page {
	index?: number;
	selection?: Selection;
	options?: SiteEntity[];
	type?: string
};

@Component({
	selector: 'upload-modal',
	templateUrl: './upload-modal.component.html',
	styleUrls: ['./upload-modal.component.css', './artifact-page.component.css'],
	animations: [
		fadeInOnEnterAnimation(),
		fadeOutOnLeaveAnimation()
	]
})
export class UploadModalComponent implements OnInit, OnDestroy {
	objectKeys = Object.keys;

	importedValues: boolean = false;

	message: string = "";

	isAdvancedSettingsCollapsed = true;

	// Make the process config type usable in the HTML template
	readonly ProcessConfigType = ProcessConfigType;

	/* 
	 * Form values
	 */
	config: UploadForm = {
		type: ProcessConfigType.ODM,
		create: false,
		resolution: 5,
		videoResolution: 4000,
		matcherNeighbors: 0,
		minNumFeatures: 10000,
		pcQuality: "MEDIUM",
		featureQuality: "HIGH",
		radiometricCalibration: "NONE",
		geoLocationFormat: "RX1R2",
		geoLocationFileName: "PIX4D.CSV",
		groundControlPointFileName: "gcp_list.txt",
		productName: null,
		processPtcloud: true,
		processDem: true,
		processOrtho: true,
		generateCopc: true
	};

	/*
	 * FineUploader for uploading large files
	 */
	uploader = null as FineUploader;

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
	differ: any;
	showFileSelectPanel: boolean = false;
	taskFinishedNotifications: any[] = [];

	/*
	 * Flag indicating if the upload should be processed by ODM
	 */
	processUpload: boolean = true;

	/*
	 * List of hierarchies
	 */
	hierarchy: string[] = [];

	component: SiteEntity = null;

	uploadTarget: string = null;

	/*
	 * List of previous selection labels
	 */
	labels: string[] = [];

	/*
	 * List of pages
	 */
	pages: Page[] = [{
		index: 0,
		selection: null,
		options: [],
		type: 'FILE',
	}];

	/*
	 * Current page  
	 */
	hierarchyChange: boolean = false;

	/*
	 * Current page  
	 */
	page: Page = this.pages[0];

	sensors: Sensor[] = [];
	platforms: Platform[] = [];

	public onUploadComplete: Subject<void>;
	public onUploadCancel: Subject<void>;

	// modalState: any = {"state":'category', "attribute":"", "termOption":""}

	modalStepConfig: StepConfig = {
		"steps": [
			{ "label": "Final", "active": true, "enabled": true }
		]
	};

	constructor(private service: ManagementService, private metadataService: MetadataService, private modalService: BsModalService, public bsModalRef: BsModalRef, differs: KeyValueDiffers) {
		this.differ = differs.find([]).create();
	}

	@ViewChild('uploader') set content(elem: ElementRef) {

		const that = this;

		if (elem != null && this.uploader == null && this.uploadTarget != null) {

			const extensions = [];

			extensions.push('zip');
			extensions.push('tar.gz');

			if (this.uploadTarget === 'ortho') {
				extensions.push('tif')
			}
			else if (this.uploadTarget === 'dem') {
				extensions.push('tif')
			}
			else if (this.uploadTarget === 'ptcloud') {
				extensions.push('laz')
				extensions.push('las')
			}

			const uiOptions: UIOptions = {
				debug: false,
				autoUpload: false,
				multiple: false,
				element: elem.nativeElement,
				template: 'qq-template',
				request: {
					endpoint: EnvironmentUtil.getApiUrl() + "/file/upload",
					forceMultipart: true
				},
				resume: {
					enabled: true,
					recordsExpireIn: 1
				},
				chunking: {
					enabled: true
				},
				retry: {
					enableAuto: true
				},
				text: {
					defaultResponseError: "Upload failed"
				},
				failedUploadTextDisplay: {
					mode: 'none'
					//responseProperty: 'error'
				},
				validation: {
					allowedExtensions: extensions
				},
				showMessage: function (message: string) {
					// 
				},
				callbacks: {
					onUpload: function (id: any, name: any): void {
						that.disabled = true;
						this.finishedTask = null;

						that.countUpload(that);

						if (that.message && that.message.length > 0) {
							that.message = "";
						}
					},
					onProgress: function (id: any, name: any, uploadedBytes: any, totalBytes: any): void {
					},
					onUploadChunk: function (id: any, name: any, chunkData: any): void {
					},
					onUploadChunkSuccess: function (id: any, chunkData: any, responseJSON: any, xhr: any): void {

						if (responseJSON.message && responseJSON.message.currentTask && !that.currentTask) {
							that.currentTask = responseJSON.message.currentTask;
						}

						if (that.currentTask && !that.pollingIsSet) {
							that.pollingIsSet = true;

							that.taskPolling = interval(2000).pipe(switchMap(() => {
								if (that.currentTask) {
									return that.service.task(that.currentTask.oid);
								}
							}))
								.pipe(map((data) => data))
								.subscribe((data) => {
									that.currentTask = data.task
								});
						}
					},
					onComplete: function (id: any, name: any, responseJSON: any, xhrOrXdr: any): void {
						that.disabled = false;
						that.finishedTask = that.currentTask;
						that.currentTask = null;
						that.existingTask = false;

						// if (!that.hierarchyChange) {
						// 	for (let i = 0; i < that.selections.length; i++) {
						// 		if (that.selections[i].isNew) {
						// 			that.hierarchyChange = true;
						// 		}
						// 	}
						// }

						if (that.taskPolling) {
							that.taskPolling.unsubscribe();
							that.pollingIsSet = false;
						}

						this.clearStoredFiles();

						clearInterval(that.uplodeCounterInterfal);

						if (responseJSON.success) {
							// let notificationMsg = "";
							// notificationMsg = "Your uploaded data is being processed into final image products. You can view the progress at the Workflow Tasks page.";

							// that.taskFinishedNotifications.push({
							//     'id': id,
							//     "message": notificationMsg
							// })

							that.bsModalRef.hide();
						}

						that.onUploadComplete.next();
					},
					onCancel: function (id: number, name: string) {
						//that.currentTask = null;

						if (that.currentTask && that.currentTask.uploadId) {
							that.service.removeUploadTask(that.currentTask.uploadId)
								.then(() => {
									this.clearStoredFiles();
								})
								.catch((err: HttpErrorResponse) => {
									this.error(err);
								});
						}

						if (that.existingTask) {
							that.page = that.pages[0];
						}

						that.disabled = false;
						that.currentTask = null;
						that.existingTask = false;

						if (that.taskPolling) {
							that.taskPolling.unsubscribe();
							that.pollingIsSet = false;
						}

						clearInterval(that.uplodeCounterInterfal);
					},
					onError: function (id: number, errorReason: string, xhrOrXdr: string) {
						that.error({ error: { message: xhrOrXdr } });
					}

				}
			};

			this.uploader = new FineUploader(uiOptions);

		}
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

		if (this.uploader) {
			const change = this.differ.diff(this.uploader);
			if (change) {
				this.setExistingTask();
			}
		}
	}

	init(component: SiteEntity, uploadTarget: string, productName?: string): void {
		this.component = component;
		this.uploadTarget = uploadTarget;

		if (productName != null) {
			this.config.productName = productName;
		}

		// this.processUpload = this.uploadTarget === 'raw';

		this.hierarchy = this.metadataService.getHierarchy();
		// this.selections = [];
		this.pages = [];

		this.pages.push({
			index: (this.pages.length),
			selection: null,
			options: [],
			type: 'SUMMARY'
		});

		this.page = this.pages[0];

		// Handle the case where there is an existing file upload
		if (this.existingTask) {
			this.page = this.pages[this.pages.length - 1];
		}

		this.config.radiometricCalibration = this.component.sensor.sensorType.isMultispectral ? "CAMERA" : "NONE";
		this.config.includeGeoLocationFile = this.component.sensor.hasGeologger === true;

		if (this.component.type.toLowerCase() === 'collection') {
			this.service.getDefaultRunConfig(this.component.id).then((config: ProcessConfig) => {
				this.config.type = config.type;
				this.config.pcQuality = config.pcQuality;
				this.config.resolution = config.resolution;
			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		}
	}

	close(): void {
		this.bsModalRef.hide();
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
		let resumable = this.uploader.getResumableFilesData() as any[];
		if (resumable.length > 0) {
			this.existingTask = true;
			//            if ( !this.selectedContinue ) {
			//                this.hideUploadPanel();
			//            }
		}
	}

	isPageValid(page: Page): boolean {
		if (page.type === 'CATEGORY') {
			if (page.selection != null) {
				if (page.selection.value != null && page.selection.value.length > 0) {
					return true;
				}
				else if (page.selection.label != null && page.selection.label.length > 0) {
					if (this.hasField('uav') && (page.selection.uav == null || page.selection.uav.length === 0)) {
						return false;
					}

					if (this.hasField('sensor') && (page.selection.sensor == null || page.selection.sensor.length === 0)) {
						return false;
					}

					if (this.hasField('collectionDate') && (page.selection.collectionDate == null || page.selection.collectionDate.length === 0)) {
						return false;
					}


					return true;
				}

				return false;
			}
		}
		else if (page.type === 'FILE') {
			if (this.uploader != null) {
				const uploads: any = this.uploader.getUploads();

				return (uploads != null && uploads.length > 0);
			};
		}
		else if (page.type === 'SUMMARY') {
			return (this.currentTask == null);
		}

		return true;
	}

	updateCurrentPageLabel(): void {
		this.page.options.forEach(entity => {
			if (entity.id === this.page.selection.value) {
				this.page.selection.label = entity.name;
			}
		})
	}

	setIsNew(isNew: boolean): void {
		this.page.selection.isNew = isNew;

		if (isNew) {
			this.page.selection.value = null;
		}
		else {
			this.page.selection.label = null;
		}
	}

	handleAddMetadata(task: Task): void {
		let modalRef = this.modalService.show(MetadataModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'upload-modal'
		});
		modalRef.content.init(task.collection);
	}

	handleNextPage(): void {

		if ((this.page.index + 1) < this.pages.length) {

			const nextPage = this.pages[this.page.index + 1];

			if (this.page.type === 'CATEGORY') {
				this.updateCurrentPageLabel();
				this.labels.push(this.page.selection.label);
			}

			if (nextPage.type === 'CATEGORY') {

				if (this.page.type === 'FILE') {
					this.page = nextPage;
				}
				else {
					if (!this.page.selection.isNew && this.page.selection.value != null && this.page.selection.value.length > 0) {

						this.service.getChildren(this.page.selection.value).then(children => {
							nextPage.options = children.filter(child => {
								return child.type === nextPage.selection.type;
							});

							this.page = nextPage;
						}).catch((err: HttpErrorResponse) => {
							this.error(err);
						});
					}
					else {
						this.page = nextPage;
					}
				}
			}
			else {
				this.page = nextPage;

				this.modalStepConfig = {
					"steps": [
						{ "label": "Category", "active": true, "enabled": false },
						{ "label": "Final", "active": true, "enabled": false }
					]
				};
			}
		}
	}

	handleBackPage(): void {

		if (this.page.index > 0) {

			const prevPage = this.pages[this.page.index - 1];

			if (prevPage.type === 'CATEGORY') {
				this.labels.splice(this.labels.length - 1, 1);

				this.modalStepConfig.steps.forEach(step => {
					if (step.label.toLowerCase() === "category" && step.enabled === false) {
						this.modalStepConfig = {
							"steps": [
								{ "label": "Category", "active": true, "enabled": true },
								{ "label": "Final", "active": true, "enabled": false }
							]
						};
					}
				})
			}

			this.page = prevPage;
		}
	}

	handleUpload(): void {
		// - IMPORTANT -
		// There is a 'hidden' usecase here (which is hard to test) which happens during upload resume (it happens when this.existingTask == true)
		// In this usecase, you MUST make sure that the uploader params are set properly, otherwise it will cause hell for our customer (but you as a dev probably won't even notice)
		// You can test this usecase, if needed, by setting the keycloak session expiration to 2 minutes and uploading a medium/large size collection.
	
		this.config.uploadTarget = this.uploadTarget;
		this.config.uasComponentOid = this.component.id;
		this.config.processUpload = this.processUpload;

		if (this.config.productName == null) {
			delete this.config.productName;
		}

		this.uploader.setParams(this.config);
		this.uploader.uploadStoredFiles();
	}



	hasField(fieldName: string): boolean {
		return this.metadataService.hasExtraField(this.page.selection.type, fieldName);
	}

	removeUpload(): void {
		const modal = this.modalService.show(BasicConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		modal.content.message = 'Are you sure you want to cancel the upload of [' + this.uploader.getResumableFilesData()[0].name + ']';
		modal.content.type = 'DANGER';
		modal.content.submitText = 'Cancel Upload';

		modal.content.onConfirm.subscribe(data => {
			this.service.removeUploadTask(this.uploader.getResumableFilesData()[0].uuid)
				.then(() => {
					//that.uploader.clearStoredFiles();
					//that.uploader.cancelAll()

					// The above clearStoredFiles() and cancelAll() methods don't appear to work so 
					// we are clearing localStorage manually.
					localStorage.clear();

					this.existingTask = false;
					this.page = this.pages[0];

					this.onUploadCancel.next();

					this.bsModalRef.hide();
				}).catch((err: HttpErrorResponse) => {
					this.error(err);
				});
		});
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
