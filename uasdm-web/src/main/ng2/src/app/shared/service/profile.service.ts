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

import { EventService } from './event.service';

import { Profile } from '../model/profile';

declare var acp: any;

@Injectable()
export class ProfileService {

    constructor( private eventService: EventService, private http: HttpClient ) { }

    get(): Promise<Profile> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        return this.http
            .post<Profile>( acp + '/account/get', { headers: headers } )
            .toPromise()
    }


    apply( profile: Profile ): Promise<Profile> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Profile>( acp + '/account/apply', JSON.stringify( { account: profile } ), { headers: headers } )
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
            .post<void>( acp + '/account/unlock', JSON.stringify( { oid: oid } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

    tasksCount(): Promise<{tasksCount:number}> {

        // status options: PROCESSING, COMPLETE, ERROR, QUEUED
        let params: HttpParams = new HttpParams();
        params = params.set('statuses', '[ERROR]');

        return this.http
            .get<{tasksCount:number}>(acp + '/project/tasks-count', { params: params })
            .toPromise()
    }
}
