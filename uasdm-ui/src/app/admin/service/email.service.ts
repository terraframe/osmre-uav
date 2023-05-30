///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';

import { Email } from '../model/email';
import { environment } from 'src/environments/environment';

@Injectable()
export class EmailService {

	constructor(private http: HttpClient, private eventService: EventService) { }

	getInstance(): Promise<Email> {

		this.eventService.start();

		return this.http.get<Email>(environment.apiUrl + '/api/email/editDefault')
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	apply(email: Email): Promise<Email> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<Email>(environment.apiUrl + '/api/email/apply', JSON.stringify(email), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}
}
