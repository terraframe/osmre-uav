///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';

import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';

import { CollectionImageSetView, ProductCriteria, ImageSet } from '../model/management';
import { environment } from 'src/environments/environment';
import { firstValueFrom } from 'rxjs';



@Injectable({ providedIn: 'root' })
export class ImageSetService {

	constructor(private http: HttpClient, private eventService: EventService) { }

	getImageSets(criteria: ProductCriteria): Promise<CollectionImageSetView[]> {
		let params: HttpParams = new HttpParams();
		params = params.set('criteria', JSON.stringify(criteria));

		return firstValueFrom(this.http.get<CollectionImageSetView[]>(environment.apiUrl + '/api/image-set/get-all', { params: params }));
	}

	list(collectionId: string): Promise<ImageSet[]> {
		let params: HttpParams = new HttpParams();
		params = params.set('collectionId', collectionId);

		return firstValueFrom(this.http.get<ImageSet[]>(environment.apiUrl + '/api/image-set/list', { params: params }));
	}

	remove(id: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return firstValueFrom(this.http
			.post<void>(environment.apiUrl + '/api/image-set/remove', JSON.stringify({ id: id }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
		)
	}

	togglePublish(id: string): Promise<ImageSet> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return firstValueFrom(this.http
			.post<ImageSet>(environment.apiUrl + '/api/image-set/toggle-publish', JSON.stringify({ id: id }), { headers: headers })
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
			.post<void>(environment.apiUrl + '/api/image-set/toggle-lock', JSON.stringify({ id: id }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
		)
	}

	create(id: string, name: string, files: string[]): Promise<ImageSet> {

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
			.post<ImageSet>(environment.apiUrl + '/api/image-set/create', JSON.stringify(params), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
		)
	}


}
