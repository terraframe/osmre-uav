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

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';
import { PageResult } from '@shared/model/page';

import { Account, User, UserInvite } from '../model/account';

declare var acp: any;

@Injectable()
export class AccountService {

    constructor( private eventService: EventService, private http: HttpClient ) { }

    page( p: number ): Promise<PageResult<User>> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'number', p.toString() );

        this.eventService.start();

        return this.http
            .get<PageResult<User>>( acp + '/uasdm-account/page', { params: params } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

    edit( oid: string ): Promise<Account> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Account>( acp + '/uasdm-account/edit', JSON.stringify( { oid: oid } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

    uasdmNewInstance(): Promise<User> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<User>( acp + '/uasdm-account/newInstance', JSON.stringify( {} ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

    newInvite(): Promise<Account> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Account>( acp + '/uasdm-account/newInvite', JSON.stringify( {} ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

    remove( oid: string ): Promise<void> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/uasdm-account/remove', JSON.stringify( { oid: oid } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

    apply( user: User, roleIds: string[] ): Promise<User> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<User>( acp + '/uasdm-account/apply', JSON.stringify( { account: user, roleIds: roleIds } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

    unlock( oid: string ): Promise<void> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/uasdm-account/unlock', JSON.stringify( { oid: oid } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

    inviteUser( invite: UserInvite, roleIds: string[] ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        console.log( "Submitting to inviteUser : ", JSON.stringify( { invite: invite, roleIds: roleIds } ) );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/uasdm-account/inviteUser', JSON.stringify( { invite: invite, roleIds: roleIds } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

    inviteComplete( user: User, token: string ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/uasdm-account/inviteComplete', JSON.stringify( { user: user, token: token } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

}
