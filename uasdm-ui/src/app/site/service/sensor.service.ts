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
import { firstValueFrom } from 'rxjs';



@Injectable()
export class SensorService implements GenericTableService {

    constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService) { }

    page(criteria: Object): Promise<PageResult<Sensor>> {
        let params: HttpParams = new HttpParams();
        params = params.set('criteria', JSON.stringify(criteria));

        return firstValueFrom(this.http
            .get<PageResult<Sensor>>(environment.apiUrl + '/api/sensor/page', { params: params }));
    }

    getAll(): Promise<{ oid: string, name: string }[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return firstValueFrom(this.http
            .get<{ oid: string, name: string }[]>(environment.apiUrl + '/api/sensor/get-all', { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
        );
    }

    get(oid: string): Promise<Sensor> {

        let params: HttpParams = new HttpParams();
        params = params.set('oid', oid);

        this.eventService.start();

        return firstValueFrom(this.http
            .get<Sensor>(environment.apiUrl + '/api/sensor/get', { params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
        );
    }

    newInstance(): Promise<Sensor> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<Sensor>(environment.apiUrl + '/api/sensor/new-instance', JSON.stringify({}), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
        );
    }

    remove(oid: string): Promise<void> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<void>(environment.apiUrl + '/api/sensor/remove', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
        );
    }

    apply(sensor: Sensor): Promise<Sensor> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return firstValueFrom(this.noErrorHttpClient
            .post<Sensor>(environment.apiUrl + '/api/sensor/apply', JSON.stringify(sensor), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
        );
    }

    search(text: string): Promise<{ oid: string, name: string }[]> {
        let params: HttpParams = new HttpParams();
        params = params.append('text', text);

        return firstValueFrom(this.http
            .get<{ oid: string, name: string }[]>(environment.apiUrl + '/api/sensor/search', { params: params })
        );
    }


}