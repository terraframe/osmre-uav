///
///
///

import { Component, OnInit, Input, OnDestroy, Inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { ErrorHandler, BasicConfirmModalComponent } from '@shared/component';

import { ProcessConfig, SiteEntity, SiteObjectsResultSet } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';
import { MetadataService } from '@site/service/metadata.service';
import { MetadataModalComponent } from './metadata-modal.component';

import {
	fadeInOnEnterAnimation,
	fadeOutOnLeaveAnimation,
	slideInLeftOnEnterAnimation,
	slideInRightOnEnterAnimation,
} from 'angular-animations';
import { ArtifactPageComponent } from './artifact-page.component';
import { RunProcessModalComponent } from './run-process-modal.component';
import { environment } from 'src/environments/environment';
import { NgIf, NgFor, NgClass } from '@angular/common';
import { CreateProductGroupModalComponent } from './create-product-group-modal.component';
import { UserAccessModalComponent } from './user-access-modal.component';
import { AuthService } from '@shared/service/auth.service';
import { ImagePreviewModalComponent } from './image-preview-modal.component';
import { TusUploadModalComponent } from './tus-upload-modal.component';
import { COLLECTION_FORMATS } from '@site/model/sensor';
import { RouterLink } from '@angular/router';
import { TabsetComponent, TabDirective } from 'ngx-bootstrap/tabs';
import { ArtifactUploadComponent } from '../artifact-upload/artifact-upload.component';
import { NgxPaginationModule } from 'ngx-pagination';
import { SafeHtmlPipe } from '@shared/pipe/safe-html.pipe';
import { IdmDatePipe } from '@shared/pipe/idmdate.pipe';
import { WebsocketService } from '@core/service/websocket.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
	standalone: true,
	selector: 'collection-modal',
	templateUrl: './collection-modal.component.html',
	styleUrls: ['./collection-modal.component.scss'],
	providers: [BasicConfirmModalComponent, ArtifactPageComponent],
	animations: [
		fadeInOnEnterAnimation(),
		fadeOutOnLeaveAnimation(),
		slideInLeftOnEnterAnimation(),
		slideInRightOnEnterAnimation(),
	],
	imports: [NgIf, RouterLink, NgFor, TabsetComponent, TabDirective, NgClass, ArtifactUploadComponent, ArtifactPageComponent, NgxPaginationModule, SafeHtmlPipe, IdmDatePipe]
})
export class CollectionModalComponent implements OnInit, OnDestroy {
	entity: SiteEntity;

	@Input()
	set initData(ins: any) {
		this.init(ins.entity, ins.folders, ins.previous)
	}

	/* 
	 * Breadcrumb of previous sites clicked on
	 */
	previous = [] as SiteEntity[];
	folders: SiteEntity[] = [];
	thumbnails: any = {};
	processRunning: boolean = false;
	message: string;
	statusMessage: string;
	processable: boolean = false;
	enableSelectableImages: boolean = false;
	tabName: string;
	showOrthoRerunMessage: boolean = false;
	canReprocessImagery: boolean = false;

	constPageSize: number = 25;

	page: SiteObjectsResultSet = new SiteObjectsResultSet();

	/*
	 * Observable subject for TreeNode changes.  Called when create is successful 
	 */
	public onNodeChange: Subject<SiteEntity>;

	video: { src: string, name: string } = { src: null, name: null };
	context: string;

	loading = false;

	constructor(
		private service: ManagementService,
		private metadataService: MetadataService,
		private modalService: BsModalService,
		public bsModalRef: BsModalRef,
		private websocketService: WebsocketService
	) {
		this.context = environment.apiUrl;

		this.websocketService.getNotifier()
			.pipe(takeUntilDestroyed())
			.subscribe((message) => {
				if (this.entity != null && message.type === "UPLOAD_JOB_CHANGE" && message.content.collection === this.entity.id) {
					if (this.tabName === 'image') {
						this.onPageChange(this.page.pageNumber);
					}
					else if (this.tabName === 'video') {
						this.getData(this.entity.id, this.tabName, null, null);
					}
				}
			});
	}

	ngOnInit(): void {
		this.onNodeChange = new Subject();

		this.page.count = 0;
		this.page.pageNumber = 1;
		this.page.pageSize = this.constPageSize;
		this.page.results = [];
	}

	ngOnDestroy(): void {
	}


	init(entity: SiteEntity, folders: SiteEntity[], previous: SiteEntity[]): void {
		this.entity = entity;
		this.folders = folders;
		this.previous = [...previous];

		if (this.previous.length > 0 && this.previous[this.previous.length - 1].id !== this.entity.id) {
			this.previous.push(this.entity);
		}

		this.onSelect("image");

		this.processable = this.metadataService.isProcessable(entity.type);
	}

	labelForFormat(format: string) {
		var meta = COLLECTION_FORMATS.find(m => m.value === format);

		return meta == null ? format : meta.label;
	}

	createImageFromBlob(image: Blob, imageData: any) {
		let reader = new FileReader();
		reader.addEventListener("load", () => {
			// this.imageToShow = reader.result;
			this.thumbnails[imageData.key] = reader.result;
		}, false);

		if (image) {
			reader.readAsDataURL(image);
		}
	}

	getThumbnail(image: SiteEntity): void {
		if (image == null) { return; }

		if (image.presignedThumbnailDownload != null && image.presignedThumbnailDownload.length > 0) {

			this.service.downloadPresigned(image.presignedThumbnailDownload, false).subscribe(blob => {
				this.createImageFromBlob(blob, image);
			}, error => {
				console.log(error);
			});

		} else {

			let rootPath: string = image.key.substr(0, image.key.lastIndexOf("/"));
			let fileName: string = /[^/]*$/.exec(image.key)[0];
			const lastPeriod: number = fileName.lastIndexOf(".");
			const thumbKey: string = rootPath + "/thumbnails/" + fileName.substr(0, lastPeriod) + ".png";

			this.service.download(image.component, thumbKey, false).subscribe(blob => {
				this.createImageFromBlob(blob, image);
			}, error => {
				console.log(error);
			});
		}
	}

	onPageChange(pageNumber: number): void {
		this.getData(this.entity.id, this.tabName, pageNumber, this.page.pageSize);
	}

	onSelect(tabName: string): void {

		this.tabName = tabName;

		if (tabName === "image") {
			this.enableSelectableImages = true;
		} else {
			this.enableSelectableImages = false;
		}

		if (tabName === "image" || tabName === "data" || tabName === "video") {
			this.page.results = [];

			let pn: number = null;
			let ps: number = null;

			if (tabName === "image") {
				if (this.page.pageNumber == null) {
					pn = 1;
				}
				else {
					pn = this.page.pageNumber;
				}
				ps = this.constPageSize;
			}

			this.video.src = null;
			this.video.name = null;

			this.getData(this.entity.id, this.tabName, pn, ps);

		}
	}

	getData(component: string, folder: string, pageNumber: number, pageSize: number) {

		this.loading = true;

		if (folder === "image" && this.entity.format && this.entity.format.startsWith('VIDEO_')) {
			folder = "video";
			pageNumber = null;
			pageSize = null;
		}

		this.service.getObjects(component, folder, pageNumber, pageSize, true).then(resultSet => {
			this.page = resultSet;

			const minNumberOfFiles = (this.entity.isLidar || (this.entity.format && this.entity.format.toLowerCase().includes("video"))) ? 0 : 1;

			this.canReprocessImagery = this.page.results.length > minNumberOfFiles ? true : false;

			for (let i = 0; i < this.page.results.length; ++i) {
				let item = this.page.results[i];

				if (this.isImage(item)) {
					this.getThumbnail(item);
				}

			}
		}).finally(() => {
			this.loading = false;
		})
	}

	isImage(item: any): boolean {
		if (item.name.toLowerCase().indexOf(".png") !== -1 || item.name.toLowerCase().indexOf(".jpg") !== -1 ||
			item.name.toLowerCase().indexOf(".jpeg") !== -1 || item.name.toLowerCase().indexOf(".tif") !== -1 ||
			item.name.toLowerCase().indexOf(".tiff") !== -1) {

			return true;
		}
		return false;
	}

	toggleExcludeImage(event: any, image: any): void {
		this.service.setExclude(image.id, !image.exclude).then(result => {
			image.exclude = result.exclude;
		});
	}

	previewImage(document: any): void {
		const rawImagePreviewModal = this.modalService.show(ImagePreviewModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: false,
			'class': 'image-preview-modal modal-xl'
		});

		rawImagePreviewModal.content.initRaw(this.entity.id, document.key, document.name);
	}

	isProcessable(item: any): boolean {
		return this.metadataService.isProcessable(item.type);
	}

	handleErosPush(): void {
		this.processRunning = true;

		this.service.pushToEros(this.entity.id).then(data => {
			this.processRunning = false;

			setTimeout(() => {
				this.showOrthoRerunMessage = false;
				this.statusMessage = "Your process is started.";
			}, 30000);
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	handleRunOrtho(): void {

		const confirmModalRef = this.modalService.show(RunProcessModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'confirmation-modal modal-xl'
		});
		confirmModalRef.content.init(this.entity);
		confirmModalRef.content.onConfirm.subscribe(configuration => {
			this.processRunning = true;
			this.showOrthoRerunMessage = true;

			// const configuration: ProcessConfig = {
			// 	type: data.type,
			// 	includeGeoLocationFile: data.includeGeoLocationFile,
			// 	outFileNamePrefix: data.outFileNamePrefix,
			// 	resolution: data.resolution,
			// 	videoResolution: data.videoResolution,
			// 	matcherNeighbors: data.matcherNeighbors,
			// 	minNumFeatures: data.minNumFeatures,
			// 	pcQuality: data.pcQuality,
			// 	featureQuality: data.featureQuality,
			// 	radiometricCalibration: data.radiometricCalibration,
			// 	geoLocationFormat: data.geoLocationFormat,
			// 	geoLocationFileName: data.geoLocationFileName,
			// 	includeGroundControlPointFile: data.includeGroundControlPointFile,
			// 	groundControlPointFileName: data.groundControlPointFileName,
			// 	productName: data.productName
			// };

			this.service.runProcess(this.entity.id, configuration).then(() => {
				this.processRunning = false;

				setTimeout(() => {
					this.showOrthoRerunMessage = false;
					this.statusMessage = "Your process is started.";
				}, 30000);
			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		});

	}

	handleCreateProductGroup(): void {

		const confirmModalRef = this.modalService.show(CreateProductGroupModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'confirmation-modal modal-xl'
		});
		confirmModalRef.content.init(this.entity);
	}


	handleDownload(): void {

		window.location.href = environment.apiUrl + '/api/project/download-all?id=' + this.entity.id + "&key=" + this.tabName;

		//      this.service.downloadAll( data.id ).then( data => {
		//        
		//      } ).catch(( err: HttpErrorResponse ) => {
		//          this.error( err );
		//      } );
	}

	handleDownloadOdmAll(): void {

		window.location.href = environment.apiUrl + '/api/project/download-odm-all?colId=' + this.entity.id;

		//      this.service.downloadAll( data.id ).then( data => {
		//        
		//      } ).catch(( err: HttpErrorResponse ) => {
		//          this.error( err );
		//      } );
	}

	handleDownloadVideo(src: string): void {

		window.location.href = src;
	}

	handleDownloadFile(item: SiteEntity): void {
		window.location.href = environment.apiUrl + '/api/project/download?id=' + this.entity.id + "&key=" + item.key;
	}

	editMetadata(): void {
		let modalRef = this.modalService.show(MetadataModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'upload-modal modal-xl'
		});
		modalRef.content.initCollection(this.entity.id, this.entity.name);

		modalRef.content.onMetadataChange.subscribe(() => {
			this.entity.metadataUploaded = true;
		});
	}

	handleAccessManagement(): void {
		let modalRef = this.modalService.show(UserAccessModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'upload-modal modal-xl'
		});
		modalRef.content.init(this.entity);
	}


	handleUpload(): void {

		const modal = this.modalService.show(TusUploadModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'upload-modal modal-xl'
		});
		modal.content.init(this.entity, "raw");

		// modal.content.onUploadComplete.subscribe(oid => {

		//   this.handleViewSite(oid);
		// });
	}


	capitalize(str): string {
		return str.replace(/^\w/, c => c.toUpperCase());
	}

	showVideo(item: SiteEntity): void {
		this.video.name = null;
		this.video.src = null;

		// We have to null out the video and then set it after angular has refereshed
		// Otherwise the video tag does not register that the src has changed.
		setTimeout(() => {
			this.video.name = item.name;
			this.video.src = environment.apiUrl + '/api/project/download?id=' + item.component + "&key=" + item.key; // + "#" + Math.random();
		}, 200);
	}

	closeVideo(): void {
		this.video.name = null;
		this.video.src = null;
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}

}
