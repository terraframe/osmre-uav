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
import { Subject } from 'rxjs';

import { Email } from '../../model/email';
import { EmailService } from '../../service/email.service';


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

	constructor(private service: EmailService) { }

	ngOnInit(): void {
        this.onSuccess = new Subject();
        
		this.service.getInstance().then(email => {
			this.email = email;
		});
	}

	onSubmit(): void {
        let that = this;
        
		this.service.apply(this.email).then(email => {
			this.email = email;
            
            that.onSuccess.next();
		});
	}
}