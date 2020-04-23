import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { ForgotPasswordService } from '../../service/forgotpassword.service';


@Component({
	selector: 'forgotpassword',
	templateUrl: './forgotpassword.component.html',
	styleUrls: ['./forgotpassword.component.css']
})
export class ForgotPasswordComponent implements OnInit {
	username: string;
	emailIsSent: boolean = false;

	constructor(private service: ForgotPasswordService, private router: Router) { }

	ngOnInit(): void {

	}

	cancel(): void {
		this.router.navigate(['login']);
	}

	onSubmit(): void {
		this.service.submit(this.username).then(response => {
			this.emailIsSent = true;
		});
	}
}
