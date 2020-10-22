import { Component, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
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

declare var acp: string;

@Component({
    selector: 'product-panel',
    templateUrl: './product-panel.component.html',
    styles: [],
    animations: [
        fadeInOnEnterAnimation(),
        fadeOutOnLeaveAnimation(),
        bounceInOnEnterAnimation(),
        bounceOutOnLeaveAnimation()
    ]
})
export class ProductPanelComponent {

    @Input() id: string;

    @Output() public toggleMapImage = new EventEmitter<Product>();

    /* 
     * List of products for the current node
     */
    products: Product[] = [];

    thumbnails: any = {};

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;


    constructor(private pService: ProductService, private mService: ManagementService, private modalService: BsModalService) { }

    ngOnChanges(changes: SimpleChanges) {

        this.refreshProducts(changes['id'].currentValue);
    }

    refreshProducts(id: string): void {
        this.products = [];
        this.thumbnails = {};

        this.pService.getProducts(id).then(products => {
            this.products = products;

            this.products.forEach(product => {
                this.getThumbnail(product);
            });
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

    getThumbnail(product: Product): void {

        // imageKey only exists if an image actually exists on s3
        if (product.imageKey) {
            const component: string = product.entities[product.entities.length - 1].id;
            const rootPath: string = product.imageKey.substr(0, product.imageKey.lastIndexOf("/"));
            const fileName: string = /[^/]*$/.exec(product.imageKey)[0];
            const lastPeriod: number = fileName.lastIndexOf(".");
            const thumbKey: string = rootPath + "/thumbnails/" + fileName.substr(0, lastPeriod) + ".png";

            this.mService.download(component, thumbKey, false).subscribe(blob => {
                this.createImageFromBlob(blob, product);
            }, error => {
                console.log(error);

                this.thumbnails[product.id] = acp + "/net/geoprism/images/thumbnail-default.png";

            });
        }
        else {
            this.thumbnails[product.id] = acp + "/net/geoprism/images/thumbnail-default.png";
        }
    }

    getDefaultImgURL(event: any): void {
        event.target.src = acp + "/net/geoprism/images/thumbnail-default.png";
    }

    handleMapIt(product: Product): void {
        this.toggleMapImage.emit(product);
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
        const component: string = product.entities[product.entities.length - 1].id;

        this.bsModalRef = this.modalService.show(ImagePreviewModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: false,
            'class': 'image-preview-modal'
        });
        this.bsModalRef.content.init(component, product.imageKey);
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
            const mapIt:boolean = product.orthoMapped;
            
            if (mapIt) {
                this.toggleMapImage.emit(product);
            }

            product.workspace = p.workspace;
            product.mapKey = p.mapKey;
            product.published = p.published;

            if (mapIt) {
                this.toggleMapImage.emit(product);
            }
        });
    }
}
