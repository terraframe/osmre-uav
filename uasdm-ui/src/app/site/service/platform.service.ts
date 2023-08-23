///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpErrorResponse, HttpParams, HttpBackend } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';
import { HttpBackendClient } from '@shared/service/http-backend-client.service';

import { PageResult } from '@shared/model/page';
import { Platform } from '../model/platform';
import { GenericTableService } from '@shared/model/generic-table';
import { environment } from 'src/environments/environment';



@Injectable()
export class PlatformService implements GenericTableService {

    constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService) { }

    page(criteria: Object): Promise<PageResult<Platform>> {
        let params: HttpParams = new HttpParams();
        params = params.set('criteria', JSON.stringify(criteria));

        return this.http
            .get<PageResult<Platform>>(environment.apiUrl + '/platform/page', { params: params })
            .toPromise();
    }

    getAll(): Promise<{ oid: string, name: string }[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return this.http
            .get<{ oid: string, name: string }[]>(environment.apiUrl + '/platform/get-all', { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }


    get(oid: string): Promise<Platform> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<Platform>(environment.apiUrl + '/platform/get', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    newInstance(): Promise<Platform> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<Platform>(environment.apiUrl + '/platform/newInstance', JSON.stringify({}), { headers: headers })
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
            .post<void>(environment.apiUrl + '/platform/remove', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    apply(platform: Platform): Promise<Platform> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.noErrorHttpClient
            .post<Platform>(environment.apiUrl + '/platform/apply', JSON.stringify({ platform: platform }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    search(text: string): Promise<{ oid: string, name: string }[]> {
        let params: HttpParams = new HttpParams();
        params = params.append('text', text);

        return this.http
            .get<{ oid: string, name: string }[]>(environment.apiUrl + '/platform/search', { params: params })
            .toPromise();
    }

}