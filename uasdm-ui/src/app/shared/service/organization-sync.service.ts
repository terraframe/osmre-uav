///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';
import { HttpBackendClient } from '@shared/service/http-backend-client.service';

import { PageResult } from '@shared/model/page';
import { GenericTableService } from '@shared/model/generic-table';
import { environment } from 'src/environments/environment';
import { OrganizationSync } from '@shared/model/organization';



@Injectable({ providedIn: 'root' })
export class OrganizationSyncService implements GenericTableService {

    constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService) { }

    getAll(): Promise<OrganizationSync[]> {
        let params: HttpParams = new HttpParams();

        return this.http
            .get<OrganizationSync[]>(environment.apiUrl + '/api/organization-synchronization/get-all', { params: params })
            .toPromise();
    }

    page(criteria: Object): Promise<PageResult<OrganizationSync>> {
        let params: HttpParams = new HttpParams();
        params = params.set('criteria', JSON.stringify(criteria));

        return this.http
            .get<PageResult<OrganizationSync>>(environment.apiUrl + '/api/organization-synchronization/page', { params: params })
            .toPromise();
    }

    get(oid: string): Promise<OrganizationSync> {

        let params: HttpParams = new HttpParams();
        params = params.set('oid', oid);

        this.eventService.start();

        return this.http
            .get<OrganizationSync>(environment.apiUrl + '/api/organization-synchronization/get', { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    newInstance(): Promise<OrganizationSync> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<OrganizationSync>(environment.apiUrl + '/api/organization-synchronization/new-instance', JSON.stringify({}), { headers: headers })
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
            .post<void>(environment.apiUrl + '/api/organization-synchronization/remove', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    execute(oid: string): Promise<void> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + '/api/organization-synchronization/execute', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }



    apply(sync: OrganizationSync): Promise<OrganizationSync> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<OrganizationSync>(environment.apiUrl + '/api/organization-synchronization/apply', JSON.stringify({ sync: sync }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }



}