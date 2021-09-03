import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpErrorResponse, HttpParams, HttpBackend } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';
import { HttpBackendClient } from '@shared/service/http-backend-client.service';

import { PageResult } from '@shared/model/page';
import { MetadataOptions, UAV } from '../model/uav';
import { GenericTableService } from '@site/model/generic-table';

declare var acp: any;

@Injectable()
export class UAVService implements GenericTableService {

    constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService) { }


    page(criteria: Object): Promise<PageResult<UAV>> {
        let params: HttpParams = new HttpParams();
        params = params.set('criteria', JSON.stringify(criteria));

        return this.http
            .get<PageResult<UAV>>(acp + '/uav/page', { params: params })
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

    search(text: string): Promise<{ oid: string, serialNumber: string, faaNumber: string }[]> {

        let params: HttpParams = new HttpParams();
        params = params.set('text', text);

        return this.http
            .get<{ oid: string, serialNumber: string, faaNumber: string }[]>(acp + '/uav/search', { params: params })
            .toPromise();
    }

    getMetadataOptions(oid: string): Promise<MetadataOptions> {

        let params: HttpParams = new HttpParams();
        params = params.set('oid', oid);

        return this.http
            .get<MetadataOptions>(acp + '/uav/get-metadata-options', { params: params })
            .toPromise();
    }
}