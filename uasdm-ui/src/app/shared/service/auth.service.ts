///
///
///

import { inject, Injectable } from '@angular/core';

import { CookieService } from 'ngx-cookie-service';
import { User } from '../model/user';
import { LocalizedValue } from '@shared/model/organization';
import { SessionService } from './session.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Store } from '@ngrx/store';
import { getUser, SessionActions } from 'src/app/state/session.state';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {

	private store = inject(Store);

	user$: Observable<User | null> = this.store.select(getUser);

	user: User | null;

	constructor(private service: CookieService, private sessionService: SessionService) {

		if (this.service.check("user")) {
			let cookieData: string = this.service.get("user")

			let cookieDataJSON: any = JSON.parse(cookieData);


			this.store.dispatch(SessionActions.setUser({
				user: {
					userName: cookieDataJSON.userName,
					roles: cookieDataJSON.roles,
					externalProfile: cookieDataJSON.externalProfile,
					loggedIn: true,
					organization: cookieDataJSON.organization
				}
			}));


		}

		this.user$.pipe(takeUntilDestroyed()).subscribe((user) => {
			this.user = user;
		});

	}

	setUser(user: User): void {
		this.user = user;
	}

	removeUser(): void {
		this.store.dispatch(SessionActions.removeUser());
	}

	getUserName(): string {
		return this.user.userName;
	}

	isLoggedIn(): boolean {
		return this.user.loggedIn;
	}

	isAdmin(): boolean {
		return this.user.roles.indexOf("geoprism.admin.Administrator") !== -1;
	}

	isExternalProfile(): boolean {
		return this.user.externalProfile;
	}

	isWorker(): boolean {
		return this.isAdmin() || this.user.roles.indexOf("geoprism.admin.DashboardBuilder") !== -1;
	}

	getOrganization(): { code: string, label: LocalizedValue } {
		return this.user.organization;
	}

}
