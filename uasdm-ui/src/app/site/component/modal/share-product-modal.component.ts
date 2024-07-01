///
///
///

import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { ErrorHandler } from '@shared/component';

import { Product, ProductDetail } from '@site/model/management';
import { ProductService } from '@site/service/product.service';

import {
	fadeInOnEnterAnimation,
	fadeOutOnLeaveAnimation,
	bounceInOnEnterAnimation,
} from 'angular-animations';
import EnvironmentUtil from '@core/utility/environment-util';

@Component({
	selector: 'share-product-modal',
	templateUrl: './share-product-modal.component.html',
	providers: [],
	styleUrls: [],
	animations: [
		fadeInOnEnterAnimation(),
		fadeOutOnLeaveAnimation(),
		bounceInOnEnterAnimation()
	]
})
export class ShareProductModalComponent {
	product: Product;

	message: string = null;
	context: string;
	items: string[] = [];

	constructor(private pService: ProductService, public bsModalRef: BsModalRef) {
		this.context = EnvironmentUtil.getApiUrl();
	}

	init(product: ProductDetail): void {
		this.product = product;

		this.pService.getMappableItems(product.id)
			.then(items => this.items = items)
			.catch(e => this.error(e));
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}

}
