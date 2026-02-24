///
///
///

import { Component, OnInit } from '@angular/core';
import { Subject } from 'rxjs';

import { Email } from '../../model/email';
import { EmailService } from '../../service/email.service';

import { BsModalRef } from 'ngx-bootstrap/modal';
import { FormsModule } from '@angular/forms';
import { LocalizeComponent } from '@shared/component/localize/localize.component';
import { NgIf } from '@angular/common';
import { LocalizePipe } from '@shared/pipe/localize.pipe';


@Component({
    standalone: true,
    selector: 'email',
    templateUrl: './email.component.html',
    styleUrls: [],
    imports: [FormsModule, LocalizeComponent, NgIf, LocalizePipe]
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
