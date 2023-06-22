///
///
///

import { CommonModule } from '@angular/common';  
import { BrowserModule } from '@angular/platform-browser';
import { Component, OnInit, Input, Output, EventEmitter, OnDestroy, Inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';

import { CollectionArtifacts, SiteEntity } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';

import { UploadModalComponent } from './upload-modal.component';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { BasicConfirmModalComponent } from '@shared/component';
import EnvironmentUtil from '@core/utility/environment-util';
import { environment } from 'src/environments/environment';
import { WebSockets } from '@core/utility/web-sockets';
import { ConfigurationService } from '@core/service/configuration.service';
import { APP_BASE_HREF } from '@angular/common';
import { ODMRunModalComponent } from './odmrun-modal.component';

@Component({
	selector: 'artifact-page',
	templateUrl: './artifact-page.component.html',
	styleUrls: [],
	providers: []
})
export class ArtifactPageComponent implements OnInit, OnDestroy {

	@Input() entity: SiteEntity;
	@Input() processRunning: boolean;
	@Input() edit: boolean = false;

	@Input() config = {
		processPtcloud: false,
		processDem: false,
		processOrtho: false
	};

	@Output() onError = new EventEmitter<HttpErrorResponse>();

	loading = false;

	artifacts: CollectionArtifacts;

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

	thumbnails: any = {};

	context: string;

	notifier: WebSocketSubject<any>;


	constructor(
		private service: ManagementService,
		private modalService: BsModalService
	) {
		this.context = EnvironmentUtil.getApiUrl();
	}

	ngOnInit(): void {

		this.notifier = webSocket(WebSockets.buildBaseUrl() + "/websocket-notifier/notify");
		this.notifier.subscribe(message => {
			if (message.type === 'UPLOAD_JOB_CHANGE'
				&& message.content.status === 'Complete'
				&& message.content.collection === this.entity.id) {
				this.loadArtifacts();
			}
		});

		this.loadArtifacts();
	}

	ngOnDestroy(): void {
		this.notifier.unsubscribe();
	}

	loadArtifacts(): void {
		this.loading = true;
		this.service.getArtifacts(this.entity.id).then(artifacts => {

			this.loading = false;

			this.artifacts = artifacts;

			this.config.processDem = (this.artifacts.dem == null || this.artifacts.dem.items.length === 0);
			this.config.processOrtho = (this.artifacts.ortho == null || this.artifacts.ortho.items.length === 0);
			this.config.processPtcloud = (this.artifacts.ptcloud == null || this.artifacts.ptcloud.items.length === 0);

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
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


	showODMRun(artifact): void {
		const modal = this.modalService.show(ODMRunModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: false,
			'class': ''
		});
		
		modal.content.initOnArtifact(artifact);
	}

	handleDownloadFile(item: SiteEntity): void {
		window.location.href = environment.apiUrl + '/project/download?id=' + this.entity.id + "&key=" + item.key;
	}


	handleUpload(folderName: string): void {

		const modal = this.modalService.show(UploadModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'upload-modal'
		});
		modal.content.init(this.entity, folderName);

		// modal.content.onUploadComplete.subscribe(oid => {

		//   this.handleViewSite(oid);
		// });
	}

	handleRemove(section: { label: string, folder: string }): void {

		const modal = this.modalService.show(BasicConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		modal.content.message = 'Do you want to delete the [' + section.label + '] products? This action cannot be undone.';
		modal.content.type = 'DANGER';
		modal.content.submitText = 'Delete';

		modal.content.onConfirm.subscribe(() => {
			this.service.removeArtifacts(this.entity.id, section.folder).then(artifacts => {
				this.artifacts = artifacts;
			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		});



		// const modal = this.modalService.show(UploadModalComponent, {
		// 	animated: true,
		// 	backdrop: true,
		// 	ignoreBackdropClick: true,
		// 	'class': 'upload-modal'
		// });
		// modal.content.init(this.entity, folderName);

		// modal.content.onUploadComplete.subscribe(oid => {

		//   this.handleViewSite(oid);
		// });
	}

	handleDownloadReport(section: { label: string, folder: string }): void {


		window.location.href = environment.apiUrl + '/project/download-report?colId=' + this.entity.id + "&folder=" + section.folder;
	}

	capitalize(str): string {
		return str.replace(/^\w/, c => c.toUpperCase());
	}

	error(err: HttpErrorResponse): void {
		this.onError.emit(err);
	}

}
