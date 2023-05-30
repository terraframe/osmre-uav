///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';

import { Application } from '../model/application';
import { environment } from 'src/environments/environment';



@Injectable()
export class HubService {

    constructor( private http: HttpClient ) { }

    applications(): Promise<Application[]> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        return this.http
            .post( environment.apiUrl + '/menu/applications', { headers: headers } )
            .toPromise()
            .then(( response: any ) => {
                return response.json() as Application[];
            } )
    }
}
