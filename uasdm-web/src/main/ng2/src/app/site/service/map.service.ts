import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { GeoJSONSource } from 'mapbox-gl';

import * as mapboxgl from 'mapbox-gl';


const mapboxKey = 'pk.eyJ1IjoidGVycmFmcmFtZSIsImEiOiJjanZxNTFnaTYyZ2RuNDlxcmNnejNtNjN6In0.-kmlS8Tgb2fNc1NPb5rJEQ';


declare var acp: any;

@Injectable()
export class MapService {

    constructor( private http: HttpClient ) {
        ( mapboxgl as any ).accessToken = mapboxKey;
    }

    features(): Promise<{ features: GeoJSONSource, bbox: number[] }> {
        let params: HttpParams = new HttpParams();

        return this.http
            .get<{ features: GeoJSONSource, bbox: number[] }>( acp + '/project/features', { params: params } )
            .toPromise()
    }

    mbForwardGeocode(searchText: string): Promise<any> {
        let params: HttpParams = new HttpParams();

        let url = "https://api.mapbox.com/geocoding/v5/mapbox.places/"+ searchText +".json?proximity=-74.70850,40.78375&access_token="+ mapboxKey;

        return this.http
            .get( url, { params: params } )
            .toPromise()
    }

}