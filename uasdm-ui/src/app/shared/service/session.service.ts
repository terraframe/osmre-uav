///
///
///

import { Injectable, OnDestroy } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from './event.service';

import { AuthService } from './auth.service';
import { User } from '../model/user';
import { environment } from 'src/environments/environment';
import { firstValueFrom, Observer, Subject, Subscription } from 'rxjs';



@Injectable({ providedIn: 'root' })
export class SessionService implements OnDestroy {

	$user: Subject<User>;

	constructor(private eventService: EventService, private http: HttpClient) {
		this.$user = new Subject();
	}

	ngOnDestroy(): void {
		this.$user.complete();
	}

	login(username: string, password: string): Promise<User> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return firstValueFrom(this.http
			.post<User>(environment.apiUrl + '/api/session/login', JSON.stringify({ username: username, password: password }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			})))
			.then((user: User) => {
				this.$user.next(user);

				return user;
			})
	}

	logout(): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return firstValueFrom(this.http
			.get<void>(environment.apiUrl + '/api/session/logout', { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			})))
			.then(() => {
				this.$user.next(null);

				return;
			})
	}

	getUser(): Subject<User> {
		return this.$user;
	}
}
