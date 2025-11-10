///
///
///

import { Component, Input, Output, EventEmitter, SimpleChanges, OnDestroy } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';
import { ImagePreviewModalComponent } from '../modal/image-preview-modal.component';
import { ProductModalComponent } from '../modal/product-modal.component';

import { CollectionProductView, Filter, Product, ProductCriteria, SELECTION_TYPE, ViewerSelection } from '@site/model/management';
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
import EnvironmentUtil from '@core/utility/environment-util';
import { AuthService } from '@shared/service/auth.service';
import { ErrorHandler } from '@shared/component';
import { LocalizedValue } from '@shared/model/organization';
import { ShareProductModalComponent } from '../modal/share-product-modal.component';

@Component({
    standalone: false,
    selector: 'product-panel',
    templateUrl: './product-panel.component.html',
    styleUrl: './product-panel.component.scss',
    animations: [
        fadeInOnEnterAnimation(),
        fadeOutOnLeaveAnimation(),
        bounceInOnEnterAnimation(),
        bounceOutOnLeaveAnimation()
    ]
})
export class ProductPanelComponent implements OnDestroy {

    @Input() selection: ViewerSelection;

    @Input() filter: Filter;

    @Input() organization?: { code: string, label: LocalizedValue };


    @Output() public toggleMapOrtho = new EventEmitter<Product>();

    @Output() public toggleMapDem = new EventEmitter<Product>();

    @Output() onViewsChange = new EventEmitter<CollectionProductView[]>();


    /* 
     * List of products for the current node
     */
    @Input() views: CollectionProductView[] = [];

    thumbnails: any = {};

    fields = [
        { label: "Name", value: "name" },
        { label: "Sensor", value: "sensor" },
        { label: "Flight Number", value: "faaNumber" },
        { label: "Serial Number", value: "serialNumber" },
        { label: "Collection Date", value: "collectionDate" }
    ];

    sortField: string = "name";

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    loading: boolean = false;

    requestId: number = 0;

    context: string;
    isAdmin: boolean = false;


    constructor(private configuration: ConfigurationService,
        private pService: ProductService,
        private mService: ManagementService,
        private modalService: BsModalService,
        private authService: AuthService) {

        this.context = EnvironmentUtil.getApiUrl();
        this.isAdmin = this.authService.isAdmin();
    }

    ngOnDestroy(): void {
        this.onViewsChange.emit([]);

        // this.views.forEach(view => {

        //     view.products.forEach(product => {
        //         if (product.orthoMapped) {
        //             this.handleMapIt(product);
        //         }
        //         if (product.demMapped) {
        //             this.handleMapDem(product);
        //         }
        //     });
        // });

    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.selection != null) {
            this.refreshProducts(changes.selection.currentValue);
        }
    }

    clipboardPublicStacUrl(product: Product, clipboardPopover) {
        const bsModalRef = this.modalService.show(ShareProductModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            class: 'modal-xl'
        });
        bsModalRef.content.init(product);

        // navigator.clipboard.writeText(product.publicStacUrl);
        // document.getElementById("PublicStacUrl-" + product.id).className = "fa fa-clipboard-check"; // glyphicon glyphicon-ok-sign
        // clipboardPopover.show();

        // setTimeout(() => {
        //     document.getElementById("PublicStacUrl-" + product.id).className = "fa fa-link";
        //     clipboardPopover.hide();
        // }, 6000);
    }

    refresh(): void {
        this.refreshProducts(this.selection);
    }

    refreshProducts(selection: ViewerSelection): void {
        this.onViewsChange.emit([]);
        this.thumbnails = {};

        this.loading = true;

        const original = ++this.requestId;

        const criteria: ProductCriteria = {
            type: selection.type,
            sortField: this.sortField,
            sortOrder: 'ASC'
        }

        if (criteria.type === SELECTION_TYPE.SITE) {
            criteria.id = selection.data.id;
        }

        let conditions = [];

        conditions.push({ field: 'organization', value: this.organization });
        conditions.push({ field: 'collectionDate', value: this.filter.collectionDate });
        conditions.push({ field: 'owner', value: this.filter.owner });
        conditions.push({ field: 'platform', value: this.filter.platform });
        conditions.push({ field: 'projectType', value: this.filter.projectType });
        conditions.push({ field: 'sensor', value: this.filter.sensor });
        conditions.push({ field: 'uav', value: this.filter.uav });
        conditions = conditions.filter(c => {
            if (c.field === 'organization') {
                return c.value != null && c.value.code.length > 0
            }

            return c.value != null && c.value.length > 0
        });

        if (criteria.type === SELECTION_TYPE.LOCATION) {
            criteria.uid = selection.data.properties.uid;
            criteria.hierarchy = selection.hierarchy;
            criteria.conditions = conditions;
        }

        this.pService.getProducts(criteria).then(views => {
            if (original === this.requestId) {

                this.loading = false;

                views.forEach(view => {
                    view.product = view.products[0];
                    view.productId = view.product.id;

                    this.getThumbnail(view.product);
                });

                this.onViewsChange.emit(views);
            }
        });
    }

    setVersion(view: CollectionProductView): void {
        if (view.product != null) {

            if (view.product.orthoMapped) {
                this.handleMapIt(view.product);
            }

            if (view.product.demMapped) {
                this.handleMapDem(view.product);
            }
        }

        view.product = view.products.find(p => p.id === view.productId);

        this.getThumbnail(view.product);
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

                this.thumbnails[product.id] = this.context + 'assets/thumbnail-default.png';

            });
        }
        else {
            this.thumbnails[product.id] = this.context + 'assets/thumbnail-default.png';
        }
    }

    getDefaultImgURL(event: any): void {
        event.target.src = this.context + 'assets/thumbnail-default.png';
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

            window.open(this.configuration.getContextPath() + "/api/pointcloud/potree/" + componentId + "/" + product.productName);
        }
    }

    handleDelete(view: CollectionProductView, event: any): void {

        event.stopPropagation();

        this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            class: 'modal-xl'
        });
        this.bsModalRef.content.message = 'Are you sure you want to delete [' + view.product.productName + ']?';
        this.bsModalRef.content.data = view.product;
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = 'Delete';

        (<BasicConfirmModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
            this.remove(view);
        });
    }

    remove(view: CollectionProductView): void {
        const productId = view.product.id;

        this.pService.remove(productId).then(response => {
            view.products = view.products.filter((n: Product) => n.id !== productId);

            if (view.products.length > 0) {
                view.product = view.products[0];
            }
            else {
                this.onViewsChange.emit(
                    this.views.filter((n: CollectionProductView) => n.componentId !== view.componentId)
                );
            }
        });
    }

    previewImage(product: Product): void {

        if (product.imageKey != null) {

            const component: string = product.entities[product.entities.length - 1].id;

            this.bsModalRef = this.modalService.show(ImagePreviewModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: false,
                'class': 'image-preview-modal modal-xl'
            });
            this.bsModalRef.content.initProduct(product.id, product.productName);
        }
    }

    handleGetInfo(product: Product): void {
        this.pService.getDetail(product.id, 1, 20).then(detail => {
            this.bsModalRef = this.modalService.show(ProductModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true,
                'class': 'product-info-modal modal-xl'
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
            product.publicStacUrl = p.publicStacUrl;

            if (mapIt) {
                this.toggleMapOrtho.emit(product);
            }
            if (demMapped) {
                this.toggleMapDem.emit(product);
            }
        });
    }

    handleToggleLock(product: Product): void {
        this.pService.toggleLock(product.id).then(() => {
            product.locked = !product.locked;
        });
    }

}
