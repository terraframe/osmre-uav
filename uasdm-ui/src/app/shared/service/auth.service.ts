///
///
///

import { Injectable } from '@angular/core';

import { CookieService } from 'ngx-cookie-service';
import { User } from '../model/user';
import { LocalizedValue } from '@shared/model/organization';

@Injectable()
export class AuthService {
	private user: User = {
		loggedIn: false,
		userName: '',
		externalProfile: false,
		roles: []
	};

	constructor(private service: CookieService) {

		if (this.service.check("user")) {
			let cookieData: string = this.service.get("user")

			let cookieDataJSON: any = JSON.parse(cookieData);

			this.user.userName = cookieDataJSON.userName;
			this.user.roles = cookieDataJSON.roles;
			this.user.externalProfile = cookieDataJSON.externalProfile;
			this.user.loggedIn = true;
			this.user.organization = cookieDataJSON.organization;
		}
	}

	setUser(user: User): void {
		this.user = user;
	}

	removeUser(): void {
		this.user = {
			loggedIn: false,
			userName: '',
			externalProfile: false,
			roles: [],
			organization: null
		};
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
