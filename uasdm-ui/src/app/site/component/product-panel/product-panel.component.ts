import { Component, Input, Output, EventEmitter, SimpleChanges, OnDestroy } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';
import { ImagePreviewModalComponent } from '../modal/image-preview-modal.component';
import { ProductModalComponent } from '../modal/product-modal.component';

import { Product } from '@site/model/management';
import { ProductService } from '@site/service/product.service';
import { ManagementService } from '@site/service/management.service';

import {
    fadeInOnEnterAnimation,
    fadeOutOnLeaveAnimation,
    bounceInOnEnterAnimation,
    bounceOutOnLeaveAnimation
} from 'angular-animations';
import { environment } from 'src/environments/environment';
import { ConfigurationService } from '@core/service/configuration.service';

@Component({
    selector: 'product-panel',
    templateUrl: './product-panel.component.html',
    animations: [
        fadeInOnEnterAnimation(),
        fadeOutOnLeaveAnimation(),
        bounceInOnEnterAnimation(),
        bounceOutOnLeaveAnimation()
    ]
})
export class ProductPanelComponent implements OnDestroy {

    @Input() id: string;

    @Output() public toggleMapOrtho = new EventEmitter<Product>();

    @Output() public toggleMapDem = new EventEmitter<Product>();

    /* 
     * List of products for the current node
     */
    products: Product[] = [];

    thumbnails: any = {};

    fields = [
        { label: "Name", value: "name" },
        { label: "Sensor", value: "sensor" },
        { label: "Flight Number", value: "faaNumber" },
        { label: "Serial Number", value: "serialNumber" },
        { label: "Product Date", value: "lastUpdateDate" }
    ];

    sortField: string = "name";

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    loading: boolean = false;

    requestId: number = 0;


    constructor(private configuration:ConfigurationService, private pService: ProductService, private mService: ManagementService, private modalService: BsModalService) { }
    
    ngOnDestroy(): void {
        this.products.forEach(product => {
            if (product.orthoMapped) {
                this.handleMapIt(product);
            }
            if (product.demMapped) {
                this.handleMapDem(product);
            }
        });
    }

    ngOnChanges(changes: SimpleChanges) {

        this.refreshProducts(changes['id'].currentValue);
    }

    refresh(): void {
        this.refreshProducts(this.id);
    }

    refreshProducts(id: string): void {
        this.products = [];
        this.thumbnails = {};

        this.loading = true;

        const original = ++this.requestId;

        this.pService.getProducts(id, this.sortField, "ASC").then(products => {
            if (original === this.requestId) {

                this.products = products;
                this.loading = false;

                this.products.forEach(product => {
                    this.getThumbnail(product);
                });
            }
        });
    }

    createImageFromBlob(image: Blob, product: Product) {
        let reader = new FileReader();
        reader.addEventListener("load", () => {
            // this.imageToShow = reader.result;
            this.thumbnails[product.id] = reader.result;
        }, false);

        if (image) {
            reader.readAsDataURL(image);
        }
    }

    hasOrthoLayer(product: Product): boolean {
        let len = product.layers.length;
        for (let i = 0; i < len; ++i) {
            if (product.layers[i].classification === 'ORTHO' && product.layers[i].key != null && product.layers[i].key.length > 0) {
                return true;
            }
        }

        return false;
    }

    hasDemLayer(product: Product): boolean {
        let len = product.layers.length;
        for (let i = 0; i < len; ++i) {
            if ((product.layers[i].classification === 'DEM_DSM' || product.layers[i].classification === 'DEM_DTM') && product.layers[i].key != null && product.layers[i].key.length > 0) {
                return true;
            }
        }

        return false;
    }

    getThumbnail(product: Product): void {

        // imageKey only exists if an image actually exists on s3
        if (product.imageKey != null) {
            const component: string = product.entities[product.entities.length - 1].id;
            const rootPath: string = product.imageKey.substr(0, product.imageKey.lastIndexOf("/"));
            const fileName: string = /[^/]*$/.exec(product.imageKey)[0];
            const lastPeriod: number = fileName.lastIndexOf(".");
            const thumbKey: string = rootPath + "/thumbnails/" + fileName.substr(0, lastPeriod) + ".png";

            this.mService.download(component, thumbKey, false).subscribe(blob => {
                this.createImageFromBlob(blob, product);
            }, error => {
                console.log(error);

                this.thumbnails[product.id] = environment.apiUrl + "/net/geoprism/images/thumbnail-default.png";

            });
        }
        else {
            this.thumbnails[product.id] = environment.apiUrl + "/net/geoprism/images/thumbnail-default.png";
        }
    }

    getDefaultImgURL(event: any): void {
        event.target.src = environment.apiUrl + "/net/geoprism/images/thumbnail-default.png";
    }

    handleMapIt(product: Product): void {
        if (this.hasOrthoLayer(product)) {
            this.toggleMapOrtho.emit(product);
        }
    }

    handleMapDem(product: Product): void {
        if (this.hasDemLayer(product)) {
            this.toggleMapDem.emit(product);
        }
    }

    handlePointcloud(product: Product): void {
        if (product.hasPointcloud) {
            let componentId: string = product.entities[product.entities.length - 1].id;

            window.open(this.configuration.getContextPath() + "/pointcloud/" + componentId + "/potree");
        }
    }

    handleDelete(product: Product, event: any): void {

        event.stopPropagation();

        this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        });
        this.bsModalRef.content.message = 'Are you sure you want to delete [' + product.name + ']?';
        this.bsModalRef.content.data = product;
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = 'Delete';

        (<BasicConfirmModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
            this.remove(data);
        });
    }

    remove(product: Product): void {
        this.pService.remove(product.id).then(response => {
            this.products = this.products.filter((n: any) => n.id !== product.id);
        });
    }

    previewImage(product: Product): void {

        if (product.imageKey != null) {

            const component: string = product.entities[product.entities.length - 1].id;

            this.bsModalRef = this.modalService.show(ImagePreviewModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: false,
                'class': 'image-preview-modal'
            });
            this.bsModalRef.content.init(product.id);
        }
    }

    handleGetInfo(product: Product): void {
        this.pService.getDetail(product.id, 1, 20).then(detail => {
            this.bsModalRef = this.modalService.show(ProductModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true,
                'class': 'product-info-modal'
            });
            this.bsModalRef.content.init(detail);
        });
    }

    handleTogglePublish(product: Product): void {
        this.pService.togglePublish(product.id).then(p => {
            const mapIt: boolean = product.orthoMapped;
            const demMapped: boolean = product.demMapped;

            if (mapIt) {
                this.toggleMapOrtho.emit(product);
            }
            if (demMapped) {
                this.toggleMapDem.emit(product);
            }

            product.published = p.published;
            product.layers = p.layers;

            if (mapIt) {
                this.toggleMapOrtho.emit(product);
            }
            if (demMapped) {
                this.toggleMapDem.emit(product);
            }
        });
    }
}
