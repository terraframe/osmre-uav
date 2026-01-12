///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';

import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';

import { CollectionRawSetView, ProductCriteria, RawSet } from '../model/management';
import { environment } from 'src/environments/environment';
import { firstValueFrom } from 'rxjs';



@Injectable()
export class RawSetService {

	constructor(private http: HttpClient, private eventService: EventService) { }

	getRawSets(criteria: ProductCriteria): Promise<CollectionRawSetView[]> {
		let params: HttpParams = new HttpParams();
		params = params.set('criteria', JSON.stringify(criteria));

		return firstValueFrom(this.http.get<CollectionRawSetView[]>(environment.apiUrl + '/api/raw-set/get-all', { params: params }));
	}

	list(collectionId: string): Promise<RawSet[]> {
		let params: HttpParams = new HttpParams();
		params = params.set('collectionId', collectionId);

		return firstValueFrom(this.http.get<RawSet[]>(environment.apiUrl + '/api/raw-set/list', { params: params }));
	}



	// getDetail(id: string, pageNumber: number, pageSize: number): Promise<ProductDetail> {
	// 	let params: HttpParams = new HttpParams();
	// 	params = params.set('id', id);
	// 	params = params.set('pageNumber', pageNumber.toString());
	// 	params = params.set('pageSize', pageSize.toString());

	// 	this.eventService.start();

	// 	return this.http
	// 		.get<ProductDetail>(environment.apiUrl + '/api/raw-set/detail', { params: params })
	// 		.pipe(finalize(() => {
	// 			this.eventService.complete();
	// 		}))
	// 		);
	// }

	remove(id: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return firstValueFrom(this.http
			.post<void>(environment.apiUrl + '/api/raw-set/remove', JSON.stringify({ id: id }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
		)
	}

	togglePublish(id: string): Promise<RawSet> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return firstValueFrom(this.http
			.post<RawSet>(environment.apiUrl + '/api/raw-set/toggle-publish', JSON.stringify({ id: id }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
		)
	}

	toggleLock(id: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return firstValueFrom(this.http
			.post<void>(environment.apiUrl + '/api/raw-set/toggle-lock', JSON.stringify({ id: id }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
		)
	}

	create(id: string, name: string, files: string[]): Promise<RawSet> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		const params = {
			collectionId: id,
			name: name,
			files: files
		};

		return firstValueFrom(this.http
			.post<RawSet>(environment.apiUrl + '/api/raw-set/create', JSON.stringify(params), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
		)
	}


}
