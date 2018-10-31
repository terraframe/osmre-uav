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
import { Component } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';

declare var acp: any;

@Component( {

    selector: 'uasdm-header',
    templateUrl: './header.component.html',
    styleUrls: []
} )
export class UasdmHeaderComponent {
    private context: string;
    private userName: string = "admin";

    constructor( private cookieService: CookieService ) {
        this.context = acp;
    }

    ngOnInit(): void {

        if ( this.cookieService.check( "user" ) ) {
            let cookieData: string = this.cookieService.get( "user" )
            let cookieDataJSON: any = JSON.parse( JSON.parse( cookieData ) );
            this.userName = cookieDataJSON.userName;
        }
        else {
            console.log('Check fails for the existence of the cookie')
            
            let cookieData: string = this.cookieService.get( "user" )
            
            if(cookieData != null) {
              let cookieDataJSON: any = JSON.parse( JSON.parse( cookieData ) );
              this.userName = cookieDataJSON.userName;                
            }
            else {
                console.log('Unable to get cookie');
            }
        }
    }

}
