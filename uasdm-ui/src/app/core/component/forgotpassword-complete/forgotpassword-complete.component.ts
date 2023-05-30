///
///
///

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ForgotPasswordService } from '../../service/forgotpassword.service';

@Component({
	templateUrl: './forgotpassword-complete.component.html',
	styleUrls: ['./forgotpassword-complete.component.css']
})
export class ForgotPasswordCompleteComponent implements OnInit {
	newPassword: string;
	token: string;
	passwordIsReset: boolean = false;
	private sub: any;

	constructor(
		private service: ForgotPasswordService,
		private router: Router,
		private route: ActivatedRoute) {
	}

	ngOnInit() {
		this.sub = this.route.params.subscribe(params => {
			this.token = params['token'];
		});
	}

	ngOnDestroy() {
		this.sub.unsubscribe();
	}


	cancel(): void {
		this.router.navigate(['login']);
	}

	onSubmit(): void {
		this.service.complete(this.newPassword, this.token).then(() => {
			this.passwordIsReset = true;
		});
	}
}
