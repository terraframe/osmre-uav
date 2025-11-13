///
///
///

import { Component, OnInit, OnDestroy, Output, EventEmitter, Input, OnChanges, SimpleChanges } from '@angular/core';

import { StacItem, StacProperty, ToggleableLayer, ToggleableLayerType } from '@site/model/layer';
import { Product } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';
import { ProductService } from '@site/service/product.service';
import { BsModalService } from 'ngx-bootstrap/modal';
import { environment } from 'src/environments/environment';
import { ProductModalComponent } from '../modal/product-modal.component';
import { KnowStacModalComponent } from '../know-stac-modal/know-stac-modal.component';


@Component({
	selector: 'imagery-panel',
	templateUrl: './imagery-panel.component.html',
	styleUrls: []
})
export class ImageryPanelComponent implements OnInit, OnDestroy, OnChanges {


	@Output() onVisibilityChange: EventEmitter<ToggleableLayer> = new EventEmitter<ToggleableLayer>();

	@Output() onGotoExtent: EventEmitter<ToggleableLayer> = new EventEmitter<ToggleableLayer>();

	@Output() onRemove: EventEmitter<ToggleableLayer> = new EventEmitter<ToggleableLayer>();

	@Output() close: EventEmitter<void> = new EventEmitter<void>();

	@Input() properties: StacProperty[] = null;

	@Input() layers: ToggleableLayer[] = [];

	context: string;

	thumbnails: any = {};

	constructor(private mService: ManagementService,
		private pService: ProductService,
		private modalService: BsModalService) {
		this.context = environment.apiUrl;
	}

	ngOnInit(): void {
		this.thumbnails = {}

		this.layers.filter(l => l.type === ToggleableLayerType.PRODUCT).forEach(l => {
			this.getThumbnail(l);
		});
	}

	ngOnDestroy(): void {
		this.thumbnails = {}
	}

	ngOnChanges(changes: SimpleChanges) {
		if (changes['layers'] != null) {
			this.thumbnails = {};

			changes['layers'].currentValue.filter(l => l.type === ToggleableLayerType.PRODUCT).forEach(l => {
				this.getThumbnail(l);
			})
		}
	}

	handleClose(): void {
		this.close.emit();
	}

	handleRemoveLayer(layer: ToggleableLayer): void {
		this.onRemove.emit(layer);
	}

	handleToggleVisibility(layer: ToggleableLayer): void {
		layer.active = !layer.active;

		this.onVisibilityChange.emit(layer);
	}

	handleGotoExtent(layer: ToggleableLayer): void {
		this.onGotoExtent.emit(layer);
	}

	createImageFromBlob(image: Blob, layer: ToggleableLayer) {
		let reader = new FileReader();
		reader.addEventListener("load", () => {
			// this.imageToShow = reader.result;
			this.thumbnails[layer.id] = reader.result;
		}, false);

		if (image) {
			reader.readAsDataURL(image);
		}
	}

	getThumbnail(layer: ToggleableLayer): void {


		// imageKey only exists if an image actually exists on s3
		const product: Product = layer.item;

		if (product.imageKey != null) {
			const component: string = product.entities[product.entities.length - 1].id;
			const rootPath: string = product.imageKey.substr(0, product.imageKey.lastIndexOf("/"));
			const fileName: string = /[^/]*$/.exec(product.imageKey)[0];
			const lastPeriod: number = fileName.lastIndexOf(".");
			const thumbKey: string = rootPath + "/thumbnails/" + fileName.substr(0, lastPeriod) + ".png";

			this.mService.download(component, thumbKey, false).subscribe(blob => {
				this.createImageFromBlob(blob, layer);
			}, error => {
				console.log(error);

				this.thumbnails[layer.id] = this.context + 'assets/thumbnail-default.png';

			});
		}
		else {
			this.thumbnails[layer.id] = this.context + 'assets/thumbnail-default.png';
		}
	}

	getDefaultImgURL(event: any): void {
		event.target.src = this.context + 'assets/thumbnail-default.png';
	}

	handleGetInfo(layer: ToggleableLayer): void {

		if (layer.type === ToggleableLayerType.PRODUCT) {
			const product: Product = layer.item;

			this.pService.getDetail(product.id, 1, 20).then(detail => {
				const bsModalRef = this.modalService.show(ProductModalComponent, {
					animated: true,
					backdrop: true,
					ignoreBackdropClick: true,
					class: 'product-info-modal modal-xl'
				});
				bsModalRef.content.init(detail);
			});
		}
		else if (layer.type === ToggleableLayerType.KNOWSTAC) {
			const item: StacItem = layer.item;

			const bsModalRef = this.modalService.show(KnowStacModalComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
				class: 'product-info-modal modal-xl'
			});
			bsModalRef.content.init(item, this.properties);
		}

	}

}
