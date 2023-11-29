///
///
///

import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { EmailComponent } from '@admin/component/email/email.component';

import { AuthService } from '@shared/service/auth.service';
import { ConfigurationService } from '@core/service/configuration.service';

@Component({
    selector: 'system-configuration',
    templateUrl: './system-configuration.component.html',
    styleUrls: []
})
export class SystemConfigurationComponent implements OnInit {
    
    userName: string = "";
    admin: boolean = false;

    private bsModalRef: BsModalRef;
    
    requireKeycloakLogin: boolean;
    
    constructor(private configuration: ConfigurationService, private modalService: BsModalService, private authService: AuthService) {
		this.requireKeycloakLogin = configuration.isRequireKeycloakLogin();
	}

    ngOnInit(): void {
        this.userName = this.authService.getUserName();
        this.admin = this.authService.isAdmin();
    }

    open(): void {
        this.bsModalRef = this.modalService.show(EmailComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        });

        (<EmailComponent>this.bsModalRef.content).onSuccess.subscribe(data => {
            this.bsModalRef.hide();
        });
    }

}
