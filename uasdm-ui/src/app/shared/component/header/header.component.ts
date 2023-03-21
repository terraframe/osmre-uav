import { Component, Input } from '@angular/core';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ProfileService } from '../../service/profile.service';
import { ProfileComponent } from '../profile/profile.component';

import { AuthService } from '../../service/auth.service';
import EnvironmentUtil from '@core/utility/environment-util';
import { Configuration } from '@core/model/application';
import { environment } from 'src/environments/environment';
import { ConfigurationService } from '@core/service/configuration.service';
import { Router } from '@angular/router';



@Component({

    selector: 'uasdm-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.css']
})
export class UasdmHeaderComponent {
    context: string;
    userName: string = "";
    admin: boolean = false;
    externalProfile: boolean = false;
    bsModalRef: BsModalRef;
    notificationCount: number = 0;

    @Input() title: string;


    constructor(
        private configuration: ConfigurationService,
        private router: Router,
        private authService: AuthService,
        private modalService: BsModalService,
        private profileService: ProfileService) {
        this.context = EnvironmentUtil.getApiUrl();
    }

    ngOnInit(): void {

        this.userName = this.authService.getUserName();
        this.admin = this.authService.isAdmin();
        this.externalProfile = this.authService.isExternalProfile();

        this.profileService.tasksCount().then(data => {

            this.notificationCount = data.tasksCount

        });
    }

    account(): void {
        this.profileService.get().then(profile => {
            this.bsModalRef = this.modalService.show(ProfileComponent, { backdrop: 'static', class: 'gray modal-lg' });
            this.bsModalRef.content.profile = profile;
        });
    }

    logout(): void {
        if (environment.production) {
            window.location.href = environment.apiUrl + "/api/session/logout";
        }
        else {
            this.configuration.logout().catch(err => {
                this.router.navigate(['/login']);
            }).then(() => {
                this.router.navigate(['/login']);
            });
        }
    }

}
