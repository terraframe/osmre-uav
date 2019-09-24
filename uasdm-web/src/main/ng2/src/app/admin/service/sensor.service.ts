import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpErrorResponse, HttpParams, HttpBackend, HttpHandler } from '@angular/common/http';

import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { EventService } from '../../shared/service/event.service';
import { HttpBackendClient } from '../../shared/service/http-backend-client.service';

import { PageResult } from '../model/account';
import { Sensor } from '../model/sensor';

declare var acp: any;

@Injectable()
export class SensorService {

    constructor( private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService ) { }

    page( p: number ): Promise<PageResult<Sensor>> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'number', p.toString() );

        this.eventService.start();

        return this.http
            .get<PageResult<Sensor>>( acp + '/sensor/page', { params: params } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    edit( oid: string ): Promise<Sensor> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Sensor>( acp + '/sensor/lock', JSON.stringify( { oid: oid } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    newInstance(): Promise<Sensor> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Sensor>( acp + '/sensor/newInstance', JSON.stringify( {} ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    remove( oid: string ): Promise<void> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/sensor/remove', JSON.stringify( { oid: oid } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    apply( sensor: Sensor ): Promise<Sensor> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.noErrorHttpClient
            .post<Sensor>( acp + '/sensor/apply', JSON.stringify( { sensor: sensor } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    unlock( oid: string ): Promise<void> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.noErrorHttpClient
            .post<void>( acp + '/sensor/unlock', JSON.stringify( { oid: oid } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }
}