import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';

import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';

import { Product, ProductDetail } from '../model/management';
import { environment } from 'src/environments/environment';



@Injectable()
export class ProductService {

	constructor(private http: HttpClient, private eventService: EventService) { }

	getProducts(id: string, sortField: string, sortOrder: string): Promise<Product[]> {
		let params: HttpParams = new HttpParams();
		params = params.set('id', id);
		params = params.set('sortField', sortField);
		params = params.set('sortOrder', sortOrder);

		return this.http.get<Product[]>(environment.apiUrl + '/product/get-all', { params: params }).toPromise();
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
}