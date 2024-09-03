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
import { LabeledPropertyGraphType, LabeledPropertyGraphTypeEntry, LabeledPropertyGraphTypeVersion, LPGSync } from '@shared/model/lpg';
import { LocalizedValue } from '@shared/model/organization';



@Injectable()
export class LPGSyncService implements GenericTableService {

    constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService) { }

    getAll(): Promise<LPGSync[]> {
        let params: HttpParams = new HttpParams();

        return this.http
            .get<LPGSync[]>(environment.apiUrl + '/api/labeled-property-graph-synchronization/get-all', { params: params })
            .toPromise();
    }

    getForOrganization(organizationCode: string): Promise<LPGSync[]> {
        let params: HttpParams = new HttpParams();
        params = params.set('organizationCode', organizationCode);

        return this.http
            .get<LPGSync[]>(environment.apiUrl + '/api/labeled-property-graph-synchronization/get-for-organization', { params: params })
            .toPromise();
    }


    page(criteria: Object): Promise<PageResult<LPGSync>> {
        let params: HttpParams = new HttpParams();
        params = params.set('criteria', JSON.stringify(criteria));

        return this.http
            .get<PageResult<LPGSync>>(environment.apiUrl + '/api/labeled-property-graph-synchronization/page', { params: params })
            .toPromise();
    }

    get(oid: string): Promise<LPGSync> {

        let params: HttpParams = new HttpParams();
        params = params.set('oid', oid);

        this.eventService.start();

        return this.http
            .get<LPGSync>(environment.apiUrl + '/api/labeled-property-graph-synchronization/get', { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getStatus(oid: string): Promise<{ status: string, error?: any }> {

        let params: HttpParams = new HttpParams();
        params = params.set('oid', oid);

        this.eventService.start();

        return this.http
            .get<{ status: string, error?: any }>(environment.apiUrl + '/api/labeled-property-graph-synchronization/get-status', { params: params })
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
            .post<LPGSync>(environment.apiUrl + '/api/labeled-property-graph-synchronization/newInstance', JSON.stringify({}), { headers: headers })
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
            .post<void>(environment.apiUrl + '/api/labeled-property-graph-synchronization/remove', JSON.stringify({ oid: oid }), { headers: headers })
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
            .post<void>(environment.apiUrl + '/api/labeled-property-graph-synchronization/execute', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    createTiles(oid: string): Promise<void> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + '/api/labeled-property-graph-synchronization/create-tiles', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }


    updateRemoteVersion(oid: string, versionId: string, versionNumber: number): Promise<LPGSync> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<LPGSync>(environment.apiUrl + '/api/labeled-property-graph-synchronization/update-remote-version', JSON.stringify({
                oid: oid,
                versionId: versionId,
                versionNumber: versionNumber
            }), { headers: headers })
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

        return this.http
            .post<LPGSync>(environment.apiUrl + '/api/labeled-property-graph-synchronization/apply', JSON.stringify({ sync: sync }), { headers: headers })
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

        return this.noErrorHttpClient
            .get<any[]>(url + 'api/labeled-property-graph-type/get-all', { params: params })
            .toPromise();
    }

    getEntries(url: string, oid: string): Promise<LabeledPropertyGraphType> {

        let params: HttpParams = new HttpParams();
        params = params.set('oid', oid);

        if (!url.endsWith('/')) {
            url += '/';
        }

        return this.noErrorHttpClient
            .get<LabeledPropertyGraphType>(url + 'api/labeled-property-graph-type/entries', { params: params })
            .toPromise();
    }

    getVersions(url: string, oid: string): Promise<LabeledPropertyGraphTypeVersion[]> {

        let params: HttpParams = new HttpParams();
        params = params.set('oid', oid);

        if (!url.endsWith('/')) {
            url += '/';
        }

        return this.noErrorHttpClient
            .get<LabeledPropertyGraphTypeVersion[]>(url + 'api/labeled-property-graph-type/versions', { params: params })
            .toPromise();
    }

    roots(oid: string): Promise<{ roots: any[], metadata: any[], parent: string }> {
        let params: HttpParams = new HttpParams();
        params = params.set('oid', oid);
        params = params.set('includeRoot', false);

        this.eventService.start();

        return this.http
            .get<{ roots: any[], metadata: any[], parent: string }>(environment.apiUrl + '/api/labeled-property-graph-synchronization/roots', { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    select(oid: string, parentType: string, parentId: string, includeMetadata: boolean,): Promise<{
        version: string,
        children: any[],
        metadata?: any[],
        envelope?: any
    }> {
        let params: HttpParams = new HttpParams();
        params = params.set('oid', oid);
        params = params.set('parentType', parentType);
        params = params.set('parentId', parentId);
        params = params.set('includeMetadata', includeMetadata);

        this.eventService.start();

        return this.http
            .get<{
                version: string,
                children: any[],
                metadata?: any[],
                envelope?: any
            }>(environment.apiUrl + '/api/labeled-property-graph-synchronization/select', { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getObject(synchronizationId: string, oid: string): Promise<any> {
        let params: HttpParams = new HttpParams();
        params = params.set('synchronizationId', synchronizationId);
        params = params.set('oid', oid);

        this.eventService.start();

        return this.http
            .get<{
                children: any[],
                metadata?: any
            }>(environment.apiUrl + '/api/labeled-property-graph-synchronization/get-object', { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }


}