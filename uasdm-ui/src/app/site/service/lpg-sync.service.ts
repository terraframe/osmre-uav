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
import { LabeledPropertyGraphType, LabeledPropertyGraphTypeEntry, LabeledPropertyGraphTypeVersion, LPGSync } from '@site/model/lpg-sync';



@Injectable()
export class LPGSyncService implements GenericTableService {

    constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService) { }


    page(criteria: Object): Promise<PageResult<LPGSync>> {
        let params: HttpParams = new HttpParams();
        params = params.set('criteria', JSON.stringify(criteria));

        return this.http
            .get<PageResult<LPGSync>>(environment.apiUrl + '/labeled-property-graph-synchronization/page', { params: params })
            .toPromise();
    }

    get(oid: string): Promise<LPGSync> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<LPGSync>(environment.apiUrl + '/labeled-property-graph-synchronization/get', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    newInstance(): Promise<LPGSync> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<LPGSync>(environment.apiUrl + '/labeled-property-graph-synchronization/newInstance', JSON.stringify({}), { headers: headers })
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
            .post<void>(environment.apiUrl + '/labeled-property-graph-synchronization/remove', JSON.stringify({ oid: oid }), { headers: headers })
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
            .post<void>(environment.apiUrl + '/labeled-property-graph-synchronization/execute', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }


    apply(sync: LPGSync): Promise<LPGSync> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.noErrorHttpClient
            .post<LPGSync>(environment.apiUrl + '/labeled-property-graph-synchronization/apply', JSON.stringify({ sync: sync }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }


    getTypes(url: string): Promise<any[]> {

        let params: HttpParams = new HttpParams();

        if (!url.endsWith('/')) {
            url += '/';
        }

        return this.http
            .get<any[]>(url + 'api/labeled-property-graph-type/get-all', { params: params })
            .toPromise();
    }

    getEntries(url: string, oid: string): Promise<LabeledPropertyGraphType> {

        let params: HttpParams = new HttpParams();
        params = params.set('oid', oid);

        if (!url.endsWith('/')) {
            url += '/';
        }

        return this.http
            .get<LabeledPropertyGraphType>(url + 'api/labeled-property-graph-type/entries', { params: params })
            .toPromise();
    }

    getVersions(url: string, oid: string): Promise<LabeledPropertyGraphTypeVersion[]> {

        let params: HttpParams = new HttpParams();
        params = params.set('oid', oid);

        if (!url.endsWith('/')) {
            url += '/';
        }

        return this.http
            .get<LabeledPropertyGraphTypeVersion[]>(url + 'api/labeled-property-graph-type/versions', { params: params })
            .toPromise();
    }

}