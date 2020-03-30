import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { map } from 'rxjs/operators';
// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from '../../shared/service/event.service';
import { HttpBackendClient } from '../../shared/service/http-backend-client.service';

import { Product, ProductDetail } from '../model/management';
import { Sensor } from '../model/sensor';
import { Platform } from '../model/platform';

declare var acp: any;

@Injectable()
export class ProductService {

    constructor( private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService ) { }

    getProducts( id: string ): Promise<Product[]> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'id', id );

        return this.http
            .get<Product[]>( acp + '/product/get-all', { params: params } )
            .toPromise()
    }

    getDetail( id: string ): Promise<ProductDetail> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'id', id );

        this.eventService.start();

        return this.http
            .get<ProductDetail>( acp + '/product/detail', { params: params } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

    remove( id: string ): Promise<void> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/product/remove', JSON.stringify( { id: id } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }


}
