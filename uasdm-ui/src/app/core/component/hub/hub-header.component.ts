///
///
///

import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { ProfileService } from '@shared/service/profile.service';
import { SessionService } from '@shared/service/session.service';

import { ProfileComponent } from '@shared/component/profile/profile.component';
import EnvironmentUtil from '@core/utility/environment-util';

@Component( {
    selector: 'hub-header',
    templateUrl: './hub-header.component.html',
    styleUrls: []
} )
export class HubHeaderComponent {
    context: string;
    @Input() isAdmin: boolean = false;
    bsModalRef: BsModalRef;

    constructor( private sessionService: SessionService,
        private modalService: BsModalService,
        private profileService: ProfileService,
        private router: Router ) {
        this.context = EnvironmentUtil.getApiUrl();
    }

    logout(): void {
        this.sessionService.logout().then( response => {
            this.router.navigate( ['/login'] );
        } );
    }

    account(): void {
        this.profileService.get().then( profile => {
            this.bsModalRef = this.modalService.show( ProfileComponent, { backdrop: 'static', class: 'gray modal-lg' } );
            this.bsModalRef.content.profile = profile;
        } );
    }

}
