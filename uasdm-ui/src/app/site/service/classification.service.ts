///
///
///

import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';

import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';
import { HttpBackendClient } from '@shared/service/http-backend-client.service';

import { PageResult } from '@shared/model/page';
import { Classification } from '@site/model/classification';
import { Injectable } from '@angular/core';
import { GenericTableService } from '@shared/model/generic-table';
import { environment } from 'src/environments/environment';

export enum Endpoint {
    SENSOR_TYPE = '/sensor-type',
    PLATFORM_TYPE = '/platform-type',
    PLATFORM_MANUFACTURER = '/platform-manufacturer',
    WAVE_LENGTH = '/wave-length'
}



@Injectable()
export class ClassificationService implements GenericTableService {

    constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService) {
    }

    page(criteria: Object, baseUrl: string): Promise<PageResult<Classification>> {

        let params: HttpParams = new HttpParams();
        params = params.set('criteria', JSON.stringify(criteria));

        return this.http
            .get<PageResult<Classification>>(environment.apiUrl + baseUrl + '/page', { params: params })
            .toPromise();
    }

    getAll(baseUrl: string): Promise<Classification[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return this.http
            .get<Classification[]>(environment.apiUrl + baseUrl + '/get-all', { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }


    get(baseUrl: string, oid: string): Promise<Classification> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<Classification>(environment.apiUrl + baseUrl + '/get', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    newInstance(baseUrl: string): Promise<Classification> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<Classification>(environment.apiUrl + baseUrl + '/newInstance', JSON.stringify({}), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    remove(baseUrl: string, oid: string): Promise<void> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + baseUrl + '/remove', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    apply(baseUrl: string, classification: Classification): Promise<Classification> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.noErrorHttpClient
            .post<Classification>(environment.apiUrl + baseUrl + '/apply', JSON.stringify({ classification: classification }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }
}