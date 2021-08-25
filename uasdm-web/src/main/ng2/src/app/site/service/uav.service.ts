import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpErrorResponse, HttpParams, HttpBackend } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';
import { HttpBackendClient } from '@shared/service/http-backend-client.service';

import { PageResult } from '@shared/model/page';
import { UAV } from '../model/uav';

declare var acp: any;

@Injectable()
export class UAVService {

    constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService) { }

    page(p: number): Promise<PageResult<UAV>> {
        let params: HttpParams = new HttpParams();
        params = params.set('number', p.toString());

        this.eventService.start();

        return this.http
            .get<PageResult<UAV>>(acp + '/uav/page', { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    get(oid: string): Promise<{ uav: UAV, bureaus: { value: string, label: string }[] }> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<{ uav: UAV, bureaus: { value: string, label: string }[] }>(acp + '/uav/get', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    newInstance(): Promise<{ uav: UAV, bureaus: { value: string, label: string }[] }> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<{ uav: UAV, bureaus: { value: string, label: string }[] }>(acp + '/uav/newInstance', JSON.stringify({}), { headers: headers })
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
            .post<void>(acp + '/uav/remove', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    apply(uav: UAV): Promise<UAV> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.noErrorHttpClient
            .post<UAV>(acp + '/uav/apply', JSON.stringify({ uav: uav }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }
}