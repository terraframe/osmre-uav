///
///
///

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from '@shared/component';

import { ForgotPasswordService } from '../../service/forgotpassword.service';
import { FormsModule } from '@angular/forms';
import { LocalizeComponent } from '@shared/component/localize/localize.component';
import { NgIf } from '@angular/common';
import { LocalizePipe } from '@shared/pipe/localize.pipe';


@Component({
    standalone: true,
    selector: 'forgotpassword',
    templateUrl: './forgotpassword.component.html',
    styleUrls: ['./forgotpassword.component.css'],
    imports: [FormsModule, LocalizeComponent, NgIf, LocalizePipe]
})
export class ForgotPasswordComponent implements OnInit {
	username: string;
	emailIsSent: boolean = false;
	message: string = null;

	constructor(private service: ForgotPasswordService, private router: Router) { }

	ngOnInit(): void {

	}

	cancel(): void {
		this.router.navigate(['login']);
	}

	onSubmit(): void {
		this.service.submit(this.username).then(response => {
			this.emailIsSent = true;
		})
		.catch((err: HttpErrorResponse) => {
            this.error(err);
        });
	}
	
	error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }
}
