///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpErrorResponse, HttpParams, HttpBackend, HttpHandler } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';
import { HttpBackendClient } from '@shared/service/http-backend-client.service';

import { PageResult } from '@shared/model/page';
import { Sensor } from '../model/sensor';
import { GenericTableService } from '@shared/model/generic-table';
import { environment } from 'src/environments/environment';



@Injectable()
export class SensorService implements GenericTableService {

    constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService) { }

    page(criteria: Object): Promise<PageResult<Sensor>> {
        let params: HttpParams = new HttpParams();
        params = params.set('criteria', JSON.stringify(criteria));

        return this.http
            .get<PageResult<Sensor>>(environment.apiUrl + '/sensor/page', { params: params })
            .toPromise();
    }

    getAll(): Promise<{ oid: string, name: string }[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return this.http
            .get<{ oid: string, name: string }[]>(environment.apiUrl + '/sensor/get-all', { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    get(oid: string): Promise<Sensor> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<Sensor>(environment.apiUrl + '/sensor/get', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    newInstance(): Promise<Sensor> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<Sensor>(environment.apiUrl + '/sensor/newInstance', JSON.stringify({}), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    remove(oid: string): Promise<void> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + '/sensor/remove', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    apply(sensor: Sensor): Promise<Sensor> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.noErrorHttpClient
            .post<Sensor>(environment.apiUrl + '/sensor/apply', JSON.stringify({ sensor: sensor }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    search(text: string): Promise<{ oid: string, name: string }[]> {
        let params: HttpParams = new HttpParams();
        params = params.append('text', text);

        return this.http
            .get<{ oid: string, name: string }[]>(environment.apiUrl + '/sensor/search', { params: params })
            .toPromise();
    }


}