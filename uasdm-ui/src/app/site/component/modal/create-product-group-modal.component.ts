///
///
///

import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, Input, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ErrorHandler } from '@shared/component';

import { ManagementService } from '@site/service/management.service';
import { SiteEntity, ODMRunConfig, Product } from '@site/model/management';
import { Subject } from 'rxjs';
import { ProductService } from '@site/service/product.service';


@Component({
    selector: 'create-product-group-modal',
    templateUrl: './create-product-group-modal.component.html',
    styleUrls: []
})
export class CreateProductGroupModalComponent {

    message: string = null;

    entity: SiteEntity = null;
    productName: string = null;


    constructor(public bsModalRef: BsModalRef, private service: ProductService) { }

    init(entity: SiteEntity) {
        this.entity = entity;
    }

    confirm(): void {

        this.service.create(this.entity.id, this.productName).then((product: Product) => {
            this.entity.hasAllZip = false;
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
