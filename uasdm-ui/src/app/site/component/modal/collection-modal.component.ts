import { Component, OnInit, Input, OnDestroy, Inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { ErrorHandler, BasicConfirmModalComponent } from '@shared/component';

import { SiteEntity, SiteObjectsResultSet } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';
import { MetadataService } from '@site/service/metadata.service';
import { MetadataModalComponent } from './metadata-modal.component';

import {
	fadeInOnEnterAnimation,
	fadeOutOnLeaveAnimation,
	slideInLeftOnEnterAnimation,
	slideInRightOnEnterAnimation,
} from 'angular-animations';
import { UploadModalComponent } from './upload-modal.component';
import { ArtifactPageComponent } from './artifact-page.component';
import { RunOrthoModalComponent } from './run-ortho-modal.component';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import EnvironmentUtil from '@core/utility/environment-util';
import { environment } from 'src/environments/environment';
import { WebSockets } from '@core/utility/web-sockets';
import { ConfigurationService } from '@core/service/configuration.service';
import { APP_BASE_HREF } from '@angular/common';

@Component({
	selector: 'collection-modal',
	templateUrl: './collection-modal.component.html',
	styleUrls: [],
	providers: [BasicConfirmModalComponent, ArtifactPageComponent],
	animations: [
		fadeInOnEnterAnimation(),
		fadeOutOnLeaveAnimation(),
		slideInLeftOnEnterAnimation(),
		slideInRightOnEnterAnimation(),
	]
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

	constPageSize: number = 50;

	page: SiteObjectsResultSet = new SiteObjectsResultSet();

	/*
	 * Observable subject for TreeNode changes.  Called when create is successful 
	 */
	public onNodeChange: Subject<SiteEntity>;

	video: { src: string, name: string } = { src: null, name: null };
	context: string;

	notifier: WebSocketSubject<any>;

	constructor(
		private service: ManagementService,
		private metadataService: MetadataService,
		private modalService: BsModalService,
		public bsModalRef: BsModalRef
	) {
		this.context = environment.apiUrl;
	}

	ngOnInit(): void {
		this.onNodeChange = new Subject();

		this.page.count = 0;
		this.page.pageNumber = 1;
		this.page.pageSize = this.constPageSize;
		this.page.results = [];

		this.notifier = webSocket(WebSockets.buildBaseUrl() + "/websocket-notifier/notify");
		this.notifier.subscribe(message => {
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

	ngOnDestroy(): void {
		this.notifier.unsubscribe();
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

	getThumbnail(image: any): void {
		if (image != null) {

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
		this.service.getObjects(component, folder, pageNumber, pageSize).then(resultSet => {
			this.page = resultSet;

			this.canReprocessImagery = this.page.results.length > 1 ? true : false;

			for (let i = 0; i < this.page.results.length; ++i) {
				let item = this.page.results[i];

				if (this.isImage(item)) {
					this.getThumbnail(item);
				}

			}
		});
	}

	isImage(item: any): boolean {
		if (item.name.toLowerCase().indexOf(".png") !== -1 || item.name.toLowerCase().indexOf(".jpg") !== -1 ||
			item.name.toLowerCase().indexOf(".jpeg") !== -1 || item.name.toLowerCase().indexOf(".tif") !== -1 ||
			item.name.toLowerCase().indexOf(".tiff") !== -1) {

			return true;
		}
		return false;
	}

	previewImage(event: any, image: any): void {
		//        this.bsModalRef = this.modalService.show( ImagePreviewModalComponent, {
		//            animated: true,
		//            backdrop: true,
		//            ignoreBackdropClick: true,
		//            'class': 'image-preview-modal'
		//        } );
		//        this.bsModalRef.content.image = image;
		//        this.bsModalRef.content.src = event.target.src;
	}

	toggleExcludeImage(event: any, image: any): void {
		this.service.setExclude(image.id, !image.exclude).then(result => {
			image.exclude = result.exclude;
		});
		//
		//
		//		if (image.exclude) {
		//			this.excludes.push(image.name);
		//		}
		//		else {
		//			let position = this.excludes.indexOf(image.name);
		//			if (position > -1) {
		//				this.excludes.splice(position, 1);
		//			}
		//		}
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

		const confirmModalRef = this.modalService.show(RunOrthoModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'confirmation-modal'
		});
		confirmModalRef.content.init(this.entity);
		confirmModalRef.content.onConfirm.subscribe(data => {
			this.processRunning = true;
			this.showOrthoRerunMessage = true;

			const configuration = {
				includeGeoLocationFile: data.includeGeoLocationFile,
				outFileNamePrefix: data.outFileName,
				resolution: data.resolution
			};

			this.service.runOrtho(this.entity.id, data.processPtcloud, data.processDem, data.processOrtho, configuration).then(() => {
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

	handleDownload(): void {

		window.location.href = environment.apiUrl + '/project/download-all?id=' + this.entity.id + "&key=" + this.tabName;

		//      this.service.downloadAll( data.id ).then( data => {
		//        
		//      } ).catch(( err: HttpErrorResponse ) => {
		//          this.error( err );
		//      } );
	}

	handleDownloadOdmAll(): void {

		window.location.href = environment.apiUrl + '/project/download-odm-all?colId=' + this.entity.id;

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
		window.location.href = environment.apiUrl + '/project/download?id=' + this.entity.id + "&key=" + item.key;
	}

	handleSetMetadata(): void {
		let modalRef = this.modalService.show(MetadataModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'upload-modal'
		});
		modalRef.content.init(this.entity.id, this.entity.name);

		modalRef.content.onMetadataChange.subscribe(() => {
			this.entity.metadataUploaded = true;
		});
	}

	handleUpload(): void {

		const modal = this.modalService.show(UploadModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'upload-modal'
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
			this.video.src = environment.apiUrl + '/project/download?id=' + item.component + "&key=" + item.key; // + "#" + Math.random();
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
