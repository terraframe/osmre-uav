import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams } from '@angular/http';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { SiteEntity } from './management';
import { EventService } from '../event/event.service';

declare var acp: any;

@Injectable()
export class ManagementService {

    constructor( private http: Http, private eventService: EventService ) { }

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

    roots(): Promise<SiteEntity[]> {
        return this.http
            .get( acp + '/project/roots' )
            .toPromise()
            .then( response => {
                return response.json() as SiteEntity[];
            } )
    }

    edit( id: string ): Promise<SiteEntity> {

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
                return response.json() as SiteEntity;
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

    newChild( parentId: string ): Promise<SiteEntity> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/project/new-child', JSON.stringify( { parentId: parentId } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as SiteEntity;
            } )
    }



    applyWithParent( entity: SiteEntity, parentId: string ): Promise<SiteEntity> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/project/apply-with-parent', JSON.stringify( { entity: entity, parentId: parentId } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as SiteEntity;
            } )
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
}
