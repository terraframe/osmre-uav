///
///
///

import { Component, OnInit } from '@angular/core';
import { Subject } from 'rxjs';

import { Email } from '../../model/email';
import { EmailService } from '../../service/email.service';

import { BsModalRef } from 'ngx-bootstrap/modal';


@Component({

	selector: 'email',
	templateUrl: './email.component.html',
	styleUrls: []
})
export class EmailComponent implements OnInit {
	public email: Email = {
		oid: '',
		server: '',
		username: '',
		password: '',
		port: 0,
		from: '',
		to: '',
	};
    
    public onSuccess: Subject<void>;

	constructor(private service: EmailService, public bsModalRef: BsModalRef) { }

	ngOnInit(): void {
        this.onSuccess = new Subject();
        
		this.service.getInstance().then(email => {
			this.email = email;
		});
	}
	
	cancel(): void {
		this.bsModalRef.hide();
	}

	onSubmit(): void {
        let that = this;
        
		this.service.apply(this.email).then(email => {
			this.email = email;
            
            that.onSuccess.next();
		});
	}
}
