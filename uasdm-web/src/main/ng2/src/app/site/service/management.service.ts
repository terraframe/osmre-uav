import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import { LngLatBounds } from 'mapbox-gl';
import { Observable } from 'rxjs';

// import 'rxjs/add/operator/toPromise';
import { finalize, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

import { AuthService } from '../../shared/service/auth.service';
import { EventService } from '../../shared/service/event.service';
import { HttpBackendClient } from '../../shared/service/http-backend-client.service';

import { SiteEntity, Message, Task, AttributeType, Condition, SiteObjectsResultSet } from '../model/management';
import { Sensor } from '../model/sensor';
import { Platform } from '../model/platform';

declare var acp: any;

@Injectable()
export class ManagementService {

    constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService, private authService: AuthService) { }

    getChildren(id: string): Promise<SiteEntity[]> {
        let params: HttpParams = new HttpParams();
        params = params.set('id', id);


        return this.http
            .get<SiteEntity[]>(acp + '/project/get-children', { params: params })
            .toPromise()
    }

    getObjects(id: string, key: string, pageNumber: number, pageSize: number): Promise<SiteObjectsResultSet> {
        let params: HttpParams = new HttpParams();
        params = params.set('id', id);

        if (key != null) {
            params = params.set('key', key);
        }

        if (pageNumber != null) {
            params = params.set('pageNumber', pageNumber.toString());
        }
        if (pageSize != null) {
            params = params.set('pageSize', pageSize.toString());
        }

        return this.http
            .get<SiteObjectsResultSet>(acp + '/project/objects', { params: params })
            .toPromise()
    }

    view(id: string): Promise<{ breadcrumbs: SiteEntity[], item: SiteEntity }> {
        let params: HttpParams = new HttpParams();
        params = params.set('id', id);

        return this.http
            .get<{ breadcrumbs: SiteEntity[], item: SiteEntity }>(acp + '/project/view', { params: params })
            .toPromise()
    }

    getItems(id: string, key: string): Promise<SiteEntity[]> {
        let params: HttpParams = new HttpParams();
        params = params.set('id', id);

        if (key != null) {
            params = params.set('key', key);
        }

        return this.http
            .get<SiteEntity[]>(acp + '/project/items', { params: params })
            .toPromise()
    }

    roots(id: string, bounds: LngLatBounds): Promise<SiteEntity[]> {
        let params: HttpParams = new HttpParams();

        if (id != null) {
            params = params.set('id', id);
        }

        if (bounds != null) {
            params = params.set('bounds', JSON.stringify(bounds));
        }

        return this.http
            .get<SiteEntity[]>(acp + '/project/roots', { params: params })
            .toPromise()
    }

    edit(id: string): Promise<{ item: SiteEntity, attributes: AttributeType[] }> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<{ item: SiteEntity, attributes: AttributeType[] }>(acp + '/project/edit', JSON.stringify({ id: id }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise()
    }

    runOrtho(id: string, excludes: string[]): Promise<{ item: SiteEntity, attributes: AttributeType[] }> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        //   this.eventService.start();

        return this.http
            .post<{ item: SiteEntity, attributes: AttributeType[] }>(acp + '/project/run-ortho', JSON.stringify({ id: id, excludes: excludes }), { headers: headers })
            .pipe(finalize(() => {
                //				this.eventService.complete();
            }))
            .toPromise()
    }

    update(entity: SiteEntity): Promise<SiteEntity> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.noErrorHttpClient
            .post<SiteEntity>(acp + '/project/update', JSON.stringify({ entity: entity }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise()
    }

    newChild(parentId: string, type: string): Promise<{ item: SiteEntity, attributes: AttributeType[] }> {

        let url = '/project/new-default-child';

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        let params = {} as any;

        if (parentId != null) {
            params.parentId = parentId;
        }

        if (type) {
            params.type = type;

            url = '/project/new-child';
        }


        this.eventService.start();


        return this.http
            .post<{ item: SiteEntity, attributes: AttributeType[] }>(acp + url, JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise()
    }



    applyWithParent(entity: SiteEntity, parentId: string): Promise<SiteEntity> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });


        let params = { entity: entity } as any;

        if (parentId != null) {
            params.parentId = parentId;
        }


        this.eventService.start();

        return this.noErrorHttpClient
            .post<SiteEntity>(acp + '/project/apply-with-parent', JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise()
    }

    getCurrentUser(): string {
        //        let userName: string = "admin";
        //
        //        if ( this.cookieService.check( "user" ) ) {
        //            let cookieData: string = this.cookieService.get( "user" )
        //            let cookieDataJSON: any = JSON.parse( JSON.parse( cookieData ) );
        //            userName = cookieDataJSON.userName;
        //        }
        //        else {
        //            console.log( 'Check fails for the existence of the cookie' )
        //
        //            let cookieData: string = this.cookieService.get( "user" )
        //
        //            if ( cookieData != null ) {
        //                let cookieDataJSON: any = JSON.parse( JSON.parse( cookieData ) );
        //                userName = cookieDataJSON.userName;
        //            }
        //            else {
        //                console.log( 'Unable to get cookie' );
        //            }
        //        }

        return this.authService.getUserName();
    }

    remove(id: string): Promise<void> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<void>(acp + '/project/remove', JSON.stringify({ id: id }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise()
    }

    removeObject(componentId: string, key: string): Promise<void> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<void>(acp + '/project/removeObject', JSON.stringify({ id: componentId, key: key }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise()
    }

    removeTask(uploadId: string): Promise<void> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<void>(acp + '/project/remove-task', JSON.stringify({ uploadId: uploadId }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise()
    }

    upload(id: string, folder: string, file: File): Promise<Document> {

        this.eventService.start();

        const formData = new FormData()
        formData.append('file', file);
        formData.append('id', id);
        formData.append('folder', folder);

        return this.http.post<Document>(acp + '/project/upload', formData)
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    tasks(): Promise<{ messages: Message[], tasks: Task[] }> {
        return this.http
            .get<{ messages: Message[], tasks: Task[] }>(acp + '/project/tasks')
            .toPromise()
    }

    task(id: string): Promise<{ messages: Message[], task: Task }> {

        let params: HttpParams = new HttpParams();
        params = params.set('id', id);

        return this.http
            .get<{ messages: Message[], task: Task }>(acp + '/project/task', { params: params })
            .toPromise()
    }

    getMissingMetadata(): Promise<Message[]> {
        return this.http
            .get<Message[]>(acp + '/project/missing-metadata')
            .toPromise()
    }

    download(id: string, key: string, useSpinner: boolean): Observable<Blob> {

        let params: HttpParams = new HttpParams();
        params = params.set('id', id);
        params = params.set('key', key);

        if (useSpinner) {
            this.eventService.start();
        }

        return this.noErrorHttpClient.get<Blob>(acp + '/project/download', { params: params, responseType: 'blob' as 'json' })
            .pipe(finalize(() => {
                if (useSpinner) {
                    this.eventService.complete();
                }
            }))
    }

    downloadAll(id: string, key: string, useSpinner: boolean): Observable<Blob> {

        let params: HttpParams = new HttpParams();
        params = params.set('id', id);
        params = params.set('key', key);

        if (useSpinner) {
            this.eventService.start();
        }

        return this.noErrorHttpClient.get<Blob>(acp + '/project/download-all', { params: params, responseType: 'blob' as 'json' })
            .pipe(finalize(() => {
                if (useSpinner) {
                    this.eventService.complete();
                }
            }))
    }

    search(terms: Observable<string>) {
        return terms
            .pipe(debounceTime(400))
            .pipe(distinctUntilChanged())
            .pipe(switchMap(term => this.searchEntries(term)));
    }

    searchEntries(term: string): Observable<string> {

        let params: HttpParams = new HttpParams();
        params = params.set('term', term);

        return this.http
            .get<string>(acp + '/project/search', { params: params })
    }

    searchEntites(term: string): Promise<any> {

        let params: HttpParams = new HttpParams();
        params = params.set('term', term);

        return this.http
            .get(acp + '/project/search', { params: params })
            .toPromise()
    }

    submitCollectionMetadata(metaObj: string): Promise<void> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.noErrorHttpClient
            .post<void>(acp + '/project/submit-metadata', JSON.stringify({ json: metaObj }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise()
    }

    getMetadataOptions(id: string): Promise<{ sensors: Sensor[], platforms: Platform[], name: string, email: string, platform: string, sensor: string }> {

        let params: HttpParams = new HttpParams();

        if (id != null) {
            params = params.set('id', id);
        }

        return this.noErrorHttpClient
            .get<{ sensors: Sensor[], platforms: Platform[], name: string, email: string, platform: string, sensor: string }>(acp + '/project/metadata-options', { params: params })
            .toPromise()
    }

    evaluate(condition: Condition, entity: SiteEntity): boolean {
        if (condition != null && condition.type === 'eq') {
            return (entity[condition.name] === condition.value);
        }
        else if (condition != null && condition.type === 'admin') {
            return this.authService.isAdmin();
        }

        return false;
    }
}
