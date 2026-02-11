///
///
///


import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';

import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service'
import { environment } from 'src/environments/environment';
import { firstValueFrom } from 'rxjs';



@Injectable({ providedIn: 'root' })
export class ForgotPasswordService {

	constructor(private http: HttpClient, private eventService: EventService) { }

	submit(username: string): Promise<void> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return firstValueFrom(this.http
			.post<void>(environment.apiUrl + '/api/forgotpassword/initiate', username, { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
		);
	}

	complete(newPassword: string, token: string): Promise<void> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return firstValueFrom(this.http
			.post<void>(environment.apiUrl + '/api/forgotpassword/complete', JSON.stringify({ newPassword: newPassword, token: token }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
		);
	}
}
