///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from './event.service';

import { AuthService } from './auth.service';
import { User } from '../model/user';
import { environment } from 'src/environments/environment';



@Injectable({ providedIn: 'root' })
export class SessionService {

	constructor(private eventService: EventService, private http: HttpClient, private authService: AuthService) {
	}

	login(username: string, password: string): Promise<User> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<User>(environment.apiUrl + '/api/session/login', JSON.stringify({ username: username, password: password }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
			.then((user: User) => {
				this.authService.setUser(user);

				return user;
			})
	}

	logout(): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.get<void>(environment.apiUrl + '/api/session/logout', { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
			.then(() => {
				this.authService.setUser(null);

				return;
			})
	}
}
