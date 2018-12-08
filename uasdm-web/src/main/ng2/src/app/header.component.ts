import { Component, Input } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { ManagementService } from './management/management.service';
import { AuthService } from './auth/auth.service';


declare var acp: any;

@Component( {

    selector: 'uasdm-header',
    templateUrl: './header.component.html',
    styleUrls: []
} )
export class UasdmHeaderComponent {
    private context: string;
    private userName: string = "";
    private admin: boolean = false;

	@Input() title: string;


    constructor( private managementService: ManagementService, private authService: AuthService ) {
        this.context = acp;
    }

    ngOnInit(): void {

        this.userName = this.managementService.getCurrentUser();
        this.admin = this.authService.isAdmin();
    }

}
