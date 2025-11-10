///
///
///

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';
import { GenericTableColumn, GenericTableConfig, TableEvent } from '@shared/model/generic-table';

import { User, Account } from '../../model/account';
import { AccountService } from '../../service/account.service';
import { AccountComponent } from './account.component';
import { AccountInviteComponent } from './account-invite.component';
import { Subject } from 'rxjs';
import { ConfigurationService } from '@core/service/configuration.service';
import { UserImportComponent } from './user-import.component';
import { AuthService } from '@shared/service/auth.service';
import { environment } from 'src/environments/environment';

@Component({
	standalone: false,
  selector: 'accounts',
	templateUrl: './accounts.component.html',
	styleUrls: ['./accounts.css']
})
export class AccountsComponent implements OnInit {

	config: GenericTableConfig;
	cols: GenericTableColumn[] = [
		{ header: 'Username', field: 'username', type: 'TEXT', sortable: true },
		{ header: 'First name', field: 'firstName', type: 'TEXT', sortable: true },
		{ header: 'Last name', field: 'lastName', type: 'TEXT', sortable: true },
		{ header: 'Phone Number', field: 'phoneNumber', type: 'TEXT', sortable: true },
		{ header: 'Email Address', field: 'email', type: 'TEXT', sortable: true },
		{ header: 'Organization', field: 'organization', type: 'TEXT', sortable: true, filter: true },
		{ header: '', type: 'ACTIONS', sortable: false },
	];
	refresh: Subject<void>;

	isAdmin: boolean = false;

	/*
	 * Reference to the modal current showing
	*/
	private bsModalRef: BsModalRef;

	requireKeycloakLogin: boolean;


	constructor(private configuration: ConfigurationService, private router: Router, private service: AccountService, private modalService: BsModalService, private authService: AuthService) {
		this.requireKeycloakLogin = configuration.isRequireKeycloakLogin();
		this.isAdmin = authService.isAdmin();
	}

	ngOnInit(): void {
		this.config = {
			service: this.service,
			remove: true,
			edit: true,
			create: true,
			label: 'User'
		}

		this.refresh = new Subject<void>();
	}

	onClick(event: TableEvent): void {
		if (event.type === 'edit') {
			this.onEdit(event.row as User);
		}
		else if (event.type === 'remove') {
			this.onRemove(event.row as User);
		}
		else if (event.type === 'create') {
			this.newInstance();
		}
	}


	onRemove(user: User): void {
		if (user.username == "admin") {
			alert("You cannot delete the admin user.");
			return;
		}

		this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
			animated: true,
			backdrop: true, class: 'modal-xl',
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.message = 'Are you sure you want to delete the user [' + user.username + ']?';
		this.bsModalRef.content.data = user;
		this.bsModalRef.content.type = 'DANGER';
		this.bsModalRef.content.submitText = 'Delete';

		(<BasicConfirmModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
			this.remove(data);
		});
	}

	remove(user: User): void {
		this.service.remove(user.oid).then(response => {
			this.refresh.next();
		});
	}

	onEdit(user: User): void {
		this.service.edit(user.oid).then(account => {
			this.bsModalRef = this.modalService.show(AccountComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
				class: 'modal-xl upload-modal'
			});
			this.bsModalRef.content.init(account);

			this.bsModalRef.content.onAccountChange.subscribe(() => {
				this.refresh.next();
			});
		});
	}

	newInstance(): void {
		this.service.newInvite().then(account => {
			this.bsModalRef = this.modalService.show(AccountComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
				class: 'modal-xl upload-modal'
			});
			this.bsModalRef.content.init(account);

			this.bsModalRef.content.onAccountChange.subscribe(() => {
				this.refresh.next();
			});
		});
	}

	inviteUsers(): void {

		this.service.newInvite().then((account: Account) => {
			this.bsModalRef = this.modalService.show(AccountInviteComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
				'class': 'modal-xl upload-modal'
			});
			this.bsModalRef.content.init(account.groups);
		});
	}

	uploadUsers(): void {
		let bsModalRef = this.modalService.show(UserImportComponent, {
			animated: true,
			backdrop: true, class: 'modal-xl',
			ignoreBackdropClick: true,
		});

		bsModalRef.content.onSuccess.subscribe(data => {
			window.location.reload();
		});
	}

	exportUsers(): void {
		window.location.href = environment.apiUrl + "/api/uasdm-account/export";
	}
}
