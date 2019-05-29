import { Component, Input } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { ManagementService } from './service/management.service';
import { AuthService } from './service/auth.service';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ProfileService } from './profile/profile.service';
import { ProfileComponent } from './profile/profile.component';

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

	@Input() title: string;


    constructor( private managementService: ManagementService, private authService: AuthService, private modalService: BsModalService, private profileService:ProfileService ) {
        this.context = acp;
    }

    ngOnInit(): void {

        this.userName = this.managementService.getCurrentUser();
        this.admin = this.authService.isAdmin();
    }
    
    account():void{
      this.profileService.get().then(profile => {
        this.bsModalRef = this.modalService.show(ProfileComponent, {backdrop: 'static', class: 'gray modal-lg'});
        this.bsModalRef.content.profile = profile;
      });
    }

}
