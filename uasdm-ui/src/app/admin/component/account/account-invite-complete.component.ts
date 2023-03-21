///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { User } from '../../model/account';

import { AccountService } from '../../service/account.service';

import { ErrorHandler, ErrorModalComponent } from '@shared/component';
import { environment } from 'src/environments/environment';

@Component({
	selector: 'account-invite-complete',
	templateUrl: './account-invite-complete.component.html',
	styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
})
export class AccountInviteCompleteComponent implements OnInit {
	user: User;
	private sub: any;
	token: string;

    /*
     * Reference to the modal current showing
    */
	private bsModalRef: BsModalRef;

	constructor(
		private service: AccountService,
		private route: ActivatedRoute,
		private modalService: BsModalService) {
	}

	ngOnInit(): void {
		this.user = new User();
		this.user.phoneNumber = '';

		this.sub = this.route.params.subscribe(params => {
			this.token = params['token'];
		});
	}

	cancel(): void {
		window.location.href = environment.apiUrl;
	}

	onSubmit(): void {
		this.service.inviteComplete(this.user, this.token).then(response => {
			window.location.href = environment.apiUrl;
		});
	}

	error(err: HttpErrorResponse): void {
		// Handle error
		if (err !== null) {
			this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
			this.bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
		}
	}

}