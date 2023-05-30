///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';
import { HttpBackendClient } from '@shared/service/http-backend-client.service';

import { PageResult } from '@shared/model/page';
import { MetadataOptions, UAV } from '../model/uav';
import { GenericTableService } from '@shared/model/generic-table';
import { environment } from 'src/environments/environment';



@Injectable()
export class UAVService implements GenericTableService {

    constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService) { }


    page(criteria: Object): Promise<PageResult<UAV>> {
        let params: HttpParams = new HttpParams();
        params = params.set('criteria', JSON.stringify(criteria));

        return this.http
            .get<PageResult<UAV>>(environment.apiUrl + '/uav/page', { params: params })
            .toPromise();
    }

    get(oid: string): Promise<{ uav: UAV, bureaus: { value: string, label: string }[] }> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<{ uav: UAV, bureaus: { value: string, label: string }[] }>(environment.apiUrl + '/uav/get', JSON.stringify({ oid: oid }), { headers: headers })
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
            .post<{ uav: UAV, bureaus: { value: string, label: string }[] }>(environment.apiUrl + '/uav/newInstance', JSON.stringify({}), { headers: headers })
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
            .post<void>(environment.apiUrl + '/uav/remove', JSON.stringify({ oid: oid }), { headers: headers })
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
            .post<UAV>(environment.apiUrl + '/uav/apply', JSON.stringify({ uav: uav }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    search(text: string, field: string): Promise<{ oid: string, serialNumber: string, faaNumber: string }[]> {

        let params: HttpParams = new HttpParams();
        params = params.set('text', text);
        params = params.set('field', field);

        return this.http
            .get<{ oid: string, serialNumber: string, faaNumber: string }[]>(environment.apiUrl + '/uav/search', { params: params })
            .toPromise();
    }

    getMetadataOptions(oid: string): Promise<MetadataOptions> {

        let params: HttpParams = new HttpParams();
        params = params.set('oid', oid);

        return this.http
            .get<MetadataOptions>(environment.apiUrl + '/uav/get-metadata-options', { params: params })
            .toPromise();
    }
}