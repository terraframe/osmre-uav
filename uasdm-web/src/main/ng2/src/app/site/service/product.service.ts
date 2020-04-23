import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';

import { finalize } from 'rxjs/operators';

import { EventService } from '../../shared/service/event.service';

import { Product, ProductDetail } from '../model/management';

declare var acp: any;

@Injectable()
export class ProductService {

	constructor(private http: HttpClient, private eventService: EventService) { }

	getProducts(id: string): Promise<Product[]> {
		let params: HttpParams = new HttpParams();
		params = params.set('id', id);

		return this.http.get<Product[]>(acp + '/product/get-all', { params: params }).toPromise();
	}

	getDetail(id: string, pageNumber: number, pageSize: number): Promise<ProductDetail> {
		let params: HttpParams = new HttpParams();
		params = params.set('id', id);
		params = params.set('pageNumber', pageNumber.toString());
		params = params.set('pageSize', pageSize.toString());

		this.eventService.start();

		return this.http
			.get<ProductDetail>(acp + '/product/detail', { params: params })
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
			.post<void>(acp + '/product/remove', JSON.stringify({ id: id }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}


}
