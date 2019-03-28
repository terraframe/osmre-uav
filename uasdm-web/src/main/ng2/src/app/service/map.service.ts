import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams, RequestOptions, ResponseContentType } from '@angular/http';
import { Observable } from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { GeoJSONSource } from 'mapbox-gl';

import * as mapboxgl from 'mapbox-gl';

declare var acp: any;

@Injectable()
export class MapService {

    constructor( private http: Http ) {
        ( mapboxgl as any ).accessToken = 'pk.eyJ1IjoiZHozMTY0MjQiLCJhIjoiNzI3NmNkOTcyNWFlNGQxNzU2OTA1N2EzN2FkNWIwMTcifQ.NS8KWg47FzfLPlKY0JMNiQ';
    }

    features(): Promise<{ features: GeoJSONSource, bbox: number[] }> {
        let params: URLSearchParams = new URLSearchParams();

        return this.http
            .get( acp + '/project/features', { search: params } )
            .toPromise()
            .then( response => {
                return response.json() as { features: GeoJSONSource, bbox: number[] };
            } )
    }


}