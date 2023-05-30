///
///
///

import { Component, OnInit } from '@angular/core';
import EnvironmentUtil from '@core/utility/environment-util';

import { AuthService } from '@shared/service/auth.service';
import { Application } from '../../model/application';
import { HubService } from '../../service/hub.service';

@Component( {
    selector: 'hub',
    templateUrl: './hub.component.html',
    styleUrls: ['./hub.component.css']
} )
export class HubComponent implements OnInit {
    context: string;
    applications: Application[] = [];
    isAdmin: boolean = false;
    buckets: string = 'col-sm-6';

    constructor( private service: HubService,
        private authService: AuthService,
    ) {

        this.context = EnvironmentUtil.getApiUrl();
    }

    ngOnInit(): void {
        this.service.applications().then( applications => {
            this.applications = applications;
        } );

        this.isAdmin = this.authService.isAdmin();
    }

    //   logout():void {
    //     this.sessionService.logout().then(response => {
    //       this.router.navigate(['/login']);	  
    //     }); 	  
    //   }

    open( application: Application ): void {
        window.location.href = this.context + '/' + application.url;
    }

    //   account():void{
    //     this.profileService.get().then(profile => {
    //       this.bsModalRef = this.modalService.show(ProfileComponent, {backdrop: 'static', class: 'gray modal-lg'});
    //       this.bsModalRef.content.profile = profile;
    //     });
    //   }
}
