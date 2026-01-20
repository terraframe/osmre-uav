///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';

import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';

import { UserAccess } from '../model/management';
import { environment } from 'src/environments/environment';
import { firstValueFrom } from 'rxjs';



@Injectable({ providedIn: 'root' })
export class UserAccessService {

	constructor(private http: HttpClient, private eventService: EventService) { }

	listUsers(componentId: string): Promise<UserAccess[]> {
		let params: HttpParams = new HttpParams();
		params = params.set('componentId', componentId);

		return firstValueFrom(this.http.get<UserAccess[]>(environment.apiUrl + '/api/user-access/list-users', { params: params }));
	}

	grantAccess(componentId: string, identifier: string): Promise<UserAccess> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		const params = {
			componentId: componentId,
			identifier: identifier
		};

		this.eventService.start();

		return firstValueFrom(this.http
			.post<UserAccess>(environment.apiUrl + '/api/user-access/grant-access', JSON.stringify(params), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			})));
	}

	removeAccess(componentId: string, identifier: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		const params = {
			componentId: componentId,
			identifier: identifier
		};

		this.eventService.start();

		return firstValueFrom(this.http
			.post<void>(environment.apiUrl + '/api/user-access/remove-access', JSON.stringify(params), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			})));
	}


}
