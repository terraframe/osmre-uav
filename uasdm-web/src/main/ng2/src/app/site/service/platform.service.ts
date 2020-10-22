import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpErrorResponse, HttpParams, HttpBackend } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';
import { HttpBackendClient } from '@shared/service/http-backend-client.service';

import { PageResult } from '@shared/model/page';
import { Platform } from '../model/platform';

declare var acp: any;

@Injectable()
export class PlatformService {

    constructor( private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService ) { }

    page( p: number ): Promise<PageResult<Platform>> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'number', p.toString() );

        this.eventService.start();

        return this.http
            .get<PageResult<Platform>>( acp + '/platform/page', { params: params } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    edit( oid: string ): Promise<Platform> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Platform>( acp + '/platform/lock', JSON.stringify( { oid: oid } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    newInstance(): Promise<Platform> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Platform>( acp + '/platform/newInstance', JSON.stringify( {} ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    remove( oid: string ): Promise<void> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/platform/remove', JSON.stringify( { oid: oid } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    apply( platform: Platform ): Promise<Platform> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.noErrorHttpClient
            .post<Platform>( acp + '/platform/apply', JSON.stringify( { platform: platform } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    unlock( oid: string ): Promise<void> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.noErrorHttpClient
            .post<void>( acp + '/platform/unlock', JSON.stringify( { oid: oid } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }
}