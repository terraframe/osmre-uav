///
///
///

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';
import { PageResult } from '@shared/model/page';

import { SessionEvent } from '../model/session-event';
import { environment } from 'src/environments/environment';
import { firstValueFrom } from 'rxjs';



@Injectable({ providedIn: 'root' })
export class SessionEventService {

	constructor(private eventService: EventService, private http: HttpClient) { }

	page(p: number): Promise<PageResult<SessionEvent>> {
		let params: HttpParams = new HttpParams();
		params = params.set('pageNumber', p.toString());
		params = params.set('pageSize', "20");

		this.eventService.start();

		return firstValueFrom(this.http
			.get<PageResult<SessionEvent>>(environment.apiUrl + '/api/session-event/page', { params: params })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
		)
	}
}
