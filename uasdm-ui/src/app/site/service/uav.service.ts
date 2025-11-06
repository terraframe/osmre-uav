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
import { firstValueFrom } from 'rxjs';



@Injectable()
export class UAVService implements GenericTableService {

    constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService) { }


    page(criteria: Object): Promise<PageResult<UAV>> {
        let params: HttpParams = new HttpParams();
        params = params.set('criteria', JSON.stringify(criteria));

        return firstValueFrom(this.http
            .get<PageResult<UAV>>(environment.apiUrl + '/api/uav/page', { params: params }));
    }

    get(oid: string): Promise<{ uav: UAV, bureaus: { value: string, label: string }[] }> {

        let params: HttpParams = new HttpParams();
        params = params.set('oid', oid);

        this.eventService.start();

        return firstValueFrom(this.http
            .get<{ uav: UAV, bureaus: { value: string, label: string }[] }>(environment.apiUrl + '/api/uav/get', { params })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    newInstance(): Promise<{ uav: UAV, bureaus: { value: string, label: string }[] }> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<{ uav: UAV, bureaus: { value: string, label: string }[] }>(environment.apiUrl + '/api/uav/new-instance', JSON.stringify({}), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    remove(oid: string): Promise<void> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<void>(environment.apiUrl + '/api/uav/remove', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    apply(uav: UAV): Promise<UAV> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return firstValueFrom(this.noErrorHttpClient
            .post<UAV>(environment.apiUrl + '/api/uav/apply', JSON.stringify(uav), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    search(text: string, field: string): Promise<{ oid: string, serialNumber: string, faaNumber: string }[]> {

        let params: HttpParams = new HttpParams();
        params = params.set('text', text);
        params = params.set('field', field);

        return firstValueFrom(this.http
            .get<{ oid: string, serialNumber: string, faaNumber: string }[]>(environment.apiUrl + '/api/uav/search', { params: params })
        );
    }

    getMetadataOptions(oid: string): Promise<MetadataOptions> {

        let params: HttpParams = new HttpParams();
        params = params.set('oid', oid);

        return firstValueFrom(this.http
            .get<MetadataOptions>(environment.apiUrl + '/api/uav/get-metadata-options', { params: params }))
    }
}