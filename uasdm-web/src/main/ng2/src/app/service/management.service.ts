import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams, RequestOptions, ResponseContentType } from '@angular/http';
import { Observable } from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { CookieService } from 'ngx-cookie-service';

import { SiteEntity, Message, Task, AttributeType } from '../model/management';
import { EventService } from './event.service';

declare var acp: any;

@Injectable()
export class ManagementService {

    constructor( private http: Http, private eventService: EventService, private cookieService: CookieService ) { }

    getChildren( id: string ): Promise<SiteEntity[]> {
        let params: URLSearchParams = new URLSearchParams();
        params.set( 'id', id );


        return this.http
            .get( acp + '/project/get-children', { search: params } )
            .toPromise()
            .then( response => {
                return response.json() as SiteEntity[];
            } )
    }

    getItems( id: string, key: string ): Promise<SiteEntity[]> {
        let params: URLSearchParams = new URLSearchParams();
        params.set( 'id', id );

        if ( key != null ) {
            params.set( 'key', key );
        }

        return this.http
            .get( acp + '/project/items', { search: params } )
            .toPromise()
            .then( response => {
                return response.json() as SiteEntity[];
            } )
    }

    roots( id: string ): Promise<SiteEntity[]> {
        let params: URLSearchParams = new URLSearchParams();

        if ( id != null ) {
            params.set( 'id', id );
        }


        return this.http
            .get( acp + '/project/roots', { search: params } )
            .toPromise()
            .then( response => {
                return response.json() as SiteEntity[];
            } )
    }

    edit( id: string ): Promise<{ item: SiteEntity, attributes: AttributeType[] }> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/project/edit', JSON.stringify( { id: id } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as { item: SiteEntity, attributes: AttributeType[] };
            } )
    }

    update( entity: SiteEntity ): Promise<SiteEntity> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/project/update', JSON.stringify( { entity: entity } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as SiteEntity;
            } )
    }

    newChild( parentId: string ): Promise<{ item: SiteEntity, attributes: AttributeType[] }> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        let params = {} as any;

        if ( parentId != null ) {
            params.parentId = parentId;
        }

        this.eventService.start();


        return this.http
            .post( acp + '/project/new-child', JSON.stringify( params ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as { item: SiteEntity, attributes: AttributeType[] };
            } )
    }



    applyWithParent( entity: SiteEntity, parentId: string ): Promise<SiteEntity> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );


        let params = { entity: entity } as any;

        if ( parentId != null ) {
            params.parentId = parentId;
        }


        this.eventService.start();

        return this.http
            .post( acp + '/project/apply-with-parent', JSON.stringify( params ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as SiteEntity;
            } )
    }

    getCurrentUser(): string {
        let userName: string = "admin";

        if ( this.cookieService.check( "user" ) ) {
            let cookieData: string = this.cookieService.get( "user" )
            let cookieDataJSON: any = JSON.parse( JSON.parse( cookieData ) );
            userName = cookieDataJSON.userName;
        }
        else {
            console.log( 'Check fails for the existence of the cookie' )

            let cookieData: string = this.cookieService.get( "user" )

            if ( cookieData != null ) {
                let cookieDataJSON: any = JSON.parse( JSON.parse( cookieData ) );
                userName = cookieDataJSON.userName;
            }
            else {
                console.log( 'Unable to get cookie' );
            }
        }

        return userName;
    }

    remove( id: string ): Promise<Response> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/project/remove', JSON.stringify( { id: id } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
    }

    removeTask( uploadId: string ): Promise<Response> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/project/remove-task', JSON.stringify( { uploadId: uploadId } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
    }

    tasks(): Promise<{ messages: Message[], tasks: Task[] }> {
        return this.http
            .get( acp + '/project/tasks' )
            .toPromise()
            .then( response => {
                return response.json() as { messages: Message[], tasks: Task[] };
            } )
    }

    task( id: string ): Promise<{ messages: Message[], task: Task }> {

        let params: URLSearchParams = new URLSearchParams();
        params.set( 'id', id );

        return this.http
            .get( acp + '/project/task', { params: params } )
            .toPromise()
            .then( response => {
                return response.json() as { messages: Message[], task: Task };
            } )
    }

    getMissingMetadata(): Promise<Message[]> {
        return this.http
            .get( acp + '/project/missing-metadata' )
            .toPromise()
            .then( response => {
                return response.json() as Message[];
            } )
    }

    download( id: string, key: string ): Observable<Blob> {

        let params: URLSearchParams = new URLSearchParams();
        params.set( 'id', id );
        params.set( 'key', key );

        let options = new RequestOptions( { responseType: ResponseContentType.Blob, search: params } );

        this.eventService.start();

        return this.http.get( acp + '/project/download', options )
            .finally(() => {
                this.eventService.complete();
            } )
            .map( res => res.blob() )
    }

    search( terms: Observable<string> ) {
        return terms.debounceTime( 400 )
            .distinctUntilChanged()
            .switchMap( term => this.searchEntries( term ) );
    }

    searchEntries( term: string ) {

        let params: URLSearchParams = new URLSearchParams();
        params.set( 'term', term );

        return this.http
            .get( acp + '/project/search', { search: params } )
            .map( res => res.json() );
    }

    searchEntites( term: string ): Promise<any> {

        let params: URLSearchParams = new URLSearchParams();
        params.set( 'term', term );

        return this.http
            .get( acp + '/project/search', { search: params } )
            .toPromise()
            .then( response => {
                return response.json();
            } )
    }
}