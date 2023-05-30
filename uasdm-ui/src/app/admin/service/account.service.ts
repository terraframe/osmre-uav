///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';
import { PageResult } from '@shared/model/page';

import { Account, User, UserInvite } from '../model/account';
import { GenericTableService } from '@shared/model/generic-table';
import { environment } from 'src/environments/environment';



@Injectable()
export class AccountService implements GenericTableService {

    constructor( private eventService: EventService, private http: HttpClient ) { }

    page( criteria: Object ): Promise<PageResult<User>> {
        let params: HttpParams = new HttpParams();
        params = params.set('criteria', JSON.stringify(criteria));

        return this.http
            .get<PageResult<User>>( environment.apiUrl + '/api/uasdm-account/page', { params: params } )
            .toPromise()
    }

    edit( oid: string ): Promise<Account> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Account>( environment.apiUrl + '/api/uasdm-account/edit', oid, { headers: headers } )
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
            .post<Account>( environment.apiUrl + '/api/uasdm-account/newInvite', "", { headers: headers } )
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
            .post<void>( environment.apiUrl + '/api/uasdm-account/remove', oid, { headers: headers } )
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
            .post<User>( environment.apiUrl + '/api/uasdm-account/apply', JSON.stringify( { account: user, roleIds: roleIds } ), { headers: headers } )
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
            .post<void>( environment.apiUrl + '/api/uasdm-account/inviteUser', JSON.stringify( { invite: invite, roleIds: roleIds } ), { headers: headers } )
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
            .post<void>( environment.apiUrl + '/api/uasdm-account/inviteComplete', JSON.stringify( { user: user, token: token } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

}
