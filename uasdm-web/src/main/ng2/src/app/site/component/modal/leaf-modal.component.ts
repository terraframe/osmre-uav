import { Component, OnInit, Input, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { BasicConfirmModalComponent } from '../../../shared/component/modal/basic-confirm-modal.component';

import { SiteEntity, SiteObjectsResultSet } from '../../model/management';
import { ManagementService } from '../../service/management.service';
import { MetadataService } from '../../service/metadata.service';

import {
	fadeInOnEnterAnimation,
	fadeOutOnLeaveAnimation,
	slideInLeftOnEnterAnimation,
	slideInRightOnEnterAnimation,
} from 'angular-animations';

declare var acp: string;

@Component({
	selector: 'leaf-modal',
	templateUrl: './leaf-modal.component.html',
	styles: [],
	providers: [BasicConfirmModalComponent],
	animations: [
		fadeInOnEnterAnimation(),
		fadeOutOnLeaveAnimation(),
		slideInLeftOnEnterAnimation(),
		slideInRightOnEnterAnimation(),
	]
})
export class LeafModalComponent implements OnInit {
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
	excludes: string[] = [];
	enableSelectableImages: boolean = false;
	folder: SiteEntity;

	constPageSize: number = 50;

	page: SiteObjectsResultSet = new SiteObjectsResultSet();

    /*
     * Reference to the modal current showing
    */
	private confirmModalRef: BsModalRef;


    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
	public onNodeChange: Subject<SiteEntity>;

	video: { src: string, name: string } = { src: null, name: null };
	defaultImage: string;

	constructor(private service: ManagementService, private metadataService: MetadataService, private modalService: BsModalService, public bsModalRef: BsModalRef) {
		this.defaultImage = acp + "/net/geoprism/images/thumbnail-default.png";
	}

	ngOnInit(): void {
		this.onNodeChange = new Subject();

		this.excludes = []; // clear excludes if toggling between tabs

		this.page.count = 0;
		this.page.pageNumber = 1;
		this.page.pageSize = this.constPageSize;
		this.page.results = [];
	}

	init(entity: SiteEntity, folders: SiteEntity[], previous: SiteEntity[]): void {
		this.entity = entity;
		this.folders = folders;
		this.previous = [...previous];

		if (this.previous.length > 0 && this.previous[this.previous.length - 1].id !== this.entity.id) {
			this.previous.push(this.entity);
		}

		if (this.folders.length > 0) {
			this.onSelect(this.folders[0]);
		}

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
		this.getData(this.folder.component, this.folder.name, pageNumber, this.page.pageSize);
	}

	onSelect(folder: SiteEntity): void {
		this.page.results = [];

		if (folder.name === "raw") {
			this.enableSelectableImages = true;
		} else {
			this.enableSelectableImages = false;
		}

		let pn: number = null;
		let ps: number = null;

		if (folder.name === "raw") {
			if (this.page.pageNumber == null) {
				pn = 1;
			}
			else {
				pn = this.page.pageNumber;
			}
			ps = this.constPageSize;
		}

		this.excludes = []; // clear excludes if toggling between tabs

		this.folder = folder;

		this.video.src = null;
		this.video.name = null;

		this.getData(folder.component, folder.name, pn, ps);
	}

	getData(component: string, folder: string, pageNumber: number, pageSize: number) {
		this.service.getObjects(component, folder, pageNumber, pageSize).then(resultSet => {
			this.page = resultSet;

			for (let i = 0; i < this.page.results.length; ++i) {
				let item = this.page.results[i];

				if (this.isImage(item)) {
					this.getThumbnail(item);

					if (this.excludes.indexOf(item.name) != -1) {
						item.excludeFromProcess = true;
					}
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
		image.excludeFromProcess = !image.excludeFromProcess;

		if (image.excludeFromProcess) {
			this.excludes.push(image.name);
		}
		else {
			let position = this.excludes.indexOf(image.name);
			if (position > -1) {
				this.excludes.splice(position, 1);
			}
		}
	}

	isProcessable(item: any): boolean {
		return this.metadataService.isProcessable(item.type);
	}

	handleRunOrtho(): void {

		// this.notificationModalRef = this.modalService.show( NotificationModalComponent, {
		//     animated: true,
		//     backdrop: true,
		//     ignoreBackdropClick: true,
		//     class: 'modal-dialog-centered'
		// } );
		// this.notificationModalRef.content.message = "Your ortho task is running for [" + this.entity.name + "]. You can view the current process and results on your tasks page.";
		// this.notificationModalRef.content.submitText = 'OK';


		event.stopPropagation();

		this.confirmModalRef = this.modalService.show(BasicConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'confirmation-modal'
		});
		this.confirmModalRef.content.message = 'Running this process will replace all output products for this ' + this.entity.type + '. Are you sure you want to re-process this data?';
		// this.bsModalRef.content.data = node;
		this.confirmModalRef.content.type = 'DANGER';
		this.confirmModalRef.content.submitText = "Run Process";

		(<BasicConfirmModalComponent>this.confirmModalRef.content).onConfirm.subscribe(data => {
			this.processRunning = true;

			this.service.runOrtho(this.entity.id, this.excludes).then(data => {
				this.processRunning = false;
				this.statusMessage = "Your process is started.";
			});
		});

	}

	handleDownload(): void {

		window.location.href = acp + '/project/download-all?id=' + this.folder.component + "&key=" + this.folder.name;

		//      this.service.downloadAll( data.id ).then( data => {
		//        
		//      } ).catch(( err: HttpErrorResponse ) => {
		//          this.error( err );
		//      } );
	}

	handleDownloadFile(src: string): void {

		window.location.href = src;
	}

	showVideo(item: SiteEntity): void {
		this.video.name = null;
		this.video.src = null;

		// We have to null out the video and then set it after angular has refereshed
		// Otherwise the video tag does not register that the src has changed.
		setTimeout(() => {
			this.video.name = item.name;
			this.video.src = acp + '/project/download?id=' + this.folder.component + "&key=" + item.key; // + "#" + Math.random();
		}, 200);
	}

	closeVideo(): void {
		this.video.name = null;
		this.video.src = null;
	}

	error(err: HttpErrorResponse): void {
		// Handle error
		if (err !== null) {
			this.message = (err.error.localizedMessage || err.error.message || err.message);
		}
	}

}
