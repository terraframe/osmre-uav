///
///
///

import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { ErrorHandler } from '@shared/component';

import { CollectionModalComponent } from './collection-modal.component'
import { ImagePreviewModalComponent } from '../modal/image-preview-modal.component';

import { ProductDetail, ProductDocument, SiteEntity } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';
import { ProductService } from '@site/service/product.service';

import {
	fadeInOnEnterAnimation,
	fadeOutOnLeaveAnimation,
	bounceInOnEnterAnimation,
} from 'angular-animations';
import { environment } from 'src/environments/environment';
import { MetadataModalComponent } from './metadata-modal.component';

@Component({
	standalone: false,
  selector: 'product-modal',
	templateUrl: './product-modal.component.html',
	providers: [CollectionModalComponent],
	styleUrls: ['./product-modal.component.css'],
	animations: [
		fadeInOnEnterAnimation(),
		fadeOutOnLeaveAnimation(),
		bounceInOnEnterAnimation()
	]
})
export class ProductModalComponent implements OnInit {
	product: ProductDetail;

	thumbnails: any = {};
	items: any[] = [];
	showSite: boolean = false;
	message: string;
	initData: any;
	rawImagePreviewModal: BsModalRef;
	activeTab: string = "images";

	constructor(private pService: ProductService, private service: ManagementService, public bsModalRef: BsModalRef, private modalService: BsModalService) { }

	ngOnInit(): void {
	}

	init(product: ProductDetail): void {
		this.product = product;

		if (this.product.imageKey) {
			this.getThumbnail(this.product.id, this.product.imageKey);
		}

		this.product.page.resultSet.forEach(pDocument => {
			this.getThumbnail(pDocument.id, pDocument.key, pDocument.presignedThumbnailDownload);
		});

		if (this.isStandalone()) {
			this.setTab("images");
		}
	}

	isStandalone(): boolean {
		return this.product.entities[this.product.entities.length-1].type.toLowerCase() !== 'collection';
	}

	// The artifact viewer expects certain metadata to be on the component. If it's a collection, it's directly on the component
	// If it's a standalone product, the metadata is available on the product. We're going to just copy our metadata onto the
	// component so that the artifact viewer doesn't need to care where it comes from.
	getArtifactPageEntity(): SiteEntity {
		if (!this.isStandalone()) { return this.product.entities[this.product.entities.length-1]; }

		let entity = JSON.parse(JSON.stringify(this.product.entities[this.product.entities.length-1]));
		entity.uav = this.product.uav;
		entity.sensor = this.product.sensor;
		return entity;
	}

	editMetadata(): void {
		let entity = this.product.entities[this.product.entities.length-1];

		let modalRef = this.modalService.show(MetadataModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'upload-modal modal-xl'
		});
		modalRef.content.initStandaloneProduct(this.product.id, this.product.productName);

		modalRef.content.onMetadataChange.subscribe(() => {
			entity.metadataUploaded = true;
		});
	}

	createImageFromBlob(image: Blob, id: string) {
		let reader = new FileReader();
		reader.addEventListener("load", () => {
			// this.imageToShow = reader.result;
			this.thumbnails[id] = reader.result;
		}, false);

		if (image) {
			reader.readAsDataURL(image);
		}
	}

	getThumbnail(id: string, key: string, presignedThumbnailDownload: string = null): void {

		if (presignedThumbnailDownload != null)
		{
			this.service.downloadPresigned(presignedThumbnailDownload, false).subscribe(blob => {
				this.createImageFromBlob(blob, id);
			}, error => {
				console.log(error);
			});
		}
		else
		{
			const component: string = this.product.entities[this.product.entities.length - 1].id;
			const rootPath: string = key.substr(0, key.lastIndexOf("/"));
			const fileName: string = /[^/]*$/.exec(key)[0];
			const lastPeriod: number = fileName.lastIndexOf(".");
			const thumbKey: string = rootPath + "/thumbnails/" + fileName.substr(0, lastPeriod) + ".png";
	
			this.service.download(component, thumbKey, false).subscribe(blob => {
				this.createImageFromBlob(blob, id);
			}, error => {
				console.log(error);
			});
		}
	}

	onPageChange(pageNumber: number): void {
		this.pService.getDetail(this.product.id, pageNumber, 20).then(detail => {
			this.init(detail);
		});
	}

	getDefaultImgURL(event: any): void {
		event.target.src = environment.apiUrl + "/assets/thumbnail-default.png";
	}

	handleGoto(): void {
		const entity = this.product.entities[this.product.entities.length - 1];
		const breadcrumbs = this.product.entities;


		this.service.getItems(entity.id, null, null).then(nodes => {
			this.initData = { "entity": entity, "folders": nodes, "previous": breadcrumbs }

			this.showSite = true;
		});

	}

	setTab(tab: string) {
		this.activeTab = tab;

		if (tab === "image" || tab === "data" || tab === "video") {
			// this.page.results = [];

			// let pn: number = null;
			// let ps: number = null;

			// if (tabName === "image") {
			// 	if (this.page.pageNumber == null) {
			// 		pn = 1;
			// 	}
			// 	else {
			// 		pn = this.page.pageNumber;
			// 	}
			// 	ps = this.constPageSize;
			// }

			// this.video.src = null;
			// this.video.name = null;

			// this.getData(this.entity.id, this.tabName, pn, ps);

		}
	}

	viewOrtho(): void {
		this.rawImagePreviewModal = this.modalService.show(ImagePreviewModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: false,
			'class': 'image-preview-modal'
		});
		this.rawImagePreviewModal.content.initProduct(this.product.id, this.product.productName);
	}

	previewImage(document: ProductDocument): void {
		const componentId: string = this.product.entities[this.product.entities.length - 1].id;

		this.rawImagePreviewModal = this.modalService.show(ImagePreviewModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: false,
			'class': 'image-preview-modal modal-xl'
		});
		this.rawImagePreviewModal.content.initRaw(componentId, document.key, document.name);
	}

    handleDownload(): void {
      //const entity = this.product.entities[this.product.entities.length - 1];        

      window.location.href = environment.apiUrl + '/api/product/get-odm-all?id=' + this.product.id;
    }


	error(err: HttpErrorResponse): void {
	  this.message = ErrorHandler.getMessageFromError(err);
	}

}
