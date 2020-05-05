import { Component, Input } from '@angular/core';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ProfileService } from '../../service/profile.service';
import { ProfileComponent } from '../profile/profile.component';

import { AuthService } from '../../service/auth.service';

declare var acp: any;

@Component( {

    selector: 'uasdm-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.css']
} )
export class UasdmHeaderComponent {
    context: string;
    userName: string = "";
    admin: boolean = false;
    bsModalRef: BsModalRef;
    notificationCount: number = 0;

    @Input() title: string;


    constructor( private authService: AuthService, private modalService: BsModalService, private profileService: ProfileService ) {
        this.context = acp;
    }

    ngOnInit(): void {

        this.userName = this.authService.getUserName();
        this.admin = this.authService.isAdmin();

        this.profileService.tasksCount().then(data => {

			this.notificationCount = data.tasksCount

		});
    }

    account(): void {
        this.profileService.get().then( profile => {
            this.bsModalRef = this.modalService.show( ProfileComponent, { backdrop: 'static', class: 'gray modal-lg' } );
            this.bsModalRef.content.profile = profile;
        } );
    }

}
