///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';

import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';

import { CollectionProductView, Product, ProductCriteria, ProductDetail } from '../model/management';
import { environment } from 'src/environments/environment';



@Injectable()
export class ProductService {

	constructor(private http: HttpClient, private eventService: EventService) { }

	getProducts(criteria: ProductCriteria): Promise<CollectionProductView[]> {
		let params: HttpParams = new HttpParams();
		params = params.set('criteria', JSON.stringify(criteria));

		return this.http.get<CollectionProductView[]>(environment.apiUrl + '/product/get-all', { params: params }).toPromise();
	}

	getDetail(id: string, pageNumber: number, pageSize: number): Promise<ProductDetail> {
		let params: HttpParams = new HttpParams();
		params = params.set('id', id);
		params = params.set('pageNumber', pageNumber.toString());
		params = params.set('pageSize', pageSize.toString());

		this.eventService.start();

		return this.http
			.get<ProductDetail>(environment.apiUrl + '/product/detail', { params: params })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	remove(id: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(environment.apiUrl + '/product/remove', JSON.stringify({ id: id }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	togglePublish(id: string): Promise<ProductDetail> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<ProductDetail>(environment.apiUrl + '/product/toggle-publish', JSON.stringify({ id: id }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	toggleLock(id: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(environment.apiUrl + '/product/toggle-lock', JSON.stringify({ id: id }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	create(id: string, productName: string): Promise<Product> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		const params = {
			collectionId: id,
			productName: productName
		};

		return this.http
			.post<Product>(environment.apiUrl + '/product/create', JSON.stringify(params), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}


}
