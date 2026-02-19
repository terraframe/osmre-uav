///
///
///

import { inject, Injectable, OnDestroy } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from './event.service';

import { User } from '../model/user';
import { environment } from 'src/environments/environment';
import { firstValueFrom } from 'rxjs';
import { Store } from '@ngrx/store';
import { SessionActions } from 'src/app/state/session.state';



@Injectable({ providedIn: 'root' })
export class SessionService implements OnDestroy {

	private store = inject(Store);

	constructor(private eventService: EventService, private http: HttpClient) {
	}

	ngOnDestroy(): void {
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
				this.store.dispatch(SessionActions.setUser({ user }));

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
				this.store.dispatch(SessionActions.removeUser());

				return;
			})
	}
}
