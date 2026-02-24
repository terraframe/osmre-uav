///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from './event.service';

import { Profile } from '../model/profile';
import { environment } from 'src/environments/environment';



@Injectable({ providedIn: 'root' })
export class ProfileService {

    constructor( private eventService: EventService, private http: HttpClient ) { }

    get(): Promise<Profile> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        return this.http
            .get<Profile>( environment.apiUrl + '/api/uasdm-account/get', { headers: headers } )
            .toPromise()
    }


    apply( profile: Profile ): Promise<Profile> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Profile>( environment.apiUrl + '/api/uasdm-account/apply', JSON.stringify( { account: profile } ), { headers: headers } )
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
            .post<void>( environment.apiUrl + '/api/uasdm-account/unlock', JSON.stringify( { oid: oid } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

    tasksCount(): Promise<{tasksCount:number}> {
        
        const statuses:string[] = ['Error', 'Failed'];

        // status options: PROCESSING, COMPLETE, ERROR, QUEUED
        let params: HttpParams = new HttpParams();
        params = params.set('statuses', JSON.stringify(statuses));

        return this.http
            .get<{tasksCount:number}>(environment.apiUrl + '/api/project/tasks-count', { params: params })
            .toPromise()
    }
}
