import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { map } from 'rxjs/operators';
// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { GeoJSONSource } from 'mapbox-gl';

// import * as mapboxgl from 'mapbox-gl';
import { environment } from 'src/environments/environment';


const mapboxKey = 'pk.eyJ1IjoidGVycmFmcmFtZSIsImEiOiJjanZxNTFnaTYyZ2RuNDlxcmNnejNtNjN6In0.-kmlS8Tgb2fNc1NPb5rJEQ';




export interface TileJson {
    bounds: [number, number, number, number],
    center: [number, number],
    maxzoom: number,
    minzoom: number,
    scheme: string,
    tilejson: string,
    tiles: any,
    version: string
}

@Injectable()
export class MapService {

    constructor(private http: HttpClient) {
        // (mapboxgl as any).accessToken = mapboxKey;
    }

    features(conditions: { field: string, value: any }[]): Promise<{ features: GeoJSONSource, bbox: number[] }> {
        let params: HttpParams = new HttpParams();
        if (conditions != null) {
            params = params.set('conditions', JSON.stringify(conditions));
        }

        return this.http
            .get<{ features: GeoJSONSource, bbox: number[] }>(environment.apiUrl + '/project/features', { params: params })
            .toPromise()
    }

    mbForwardGeocode(searchText: string): Promise<any> {
        let params: HttpParams = new HttpParams();

        let url = "https://api.mapbox.com/geocoding/v5/mapbox.places/" + searchText + ".json?proximity=-74.70850,40.78375&access_token=" + mapboxKey;

        return this.http
            .get(url, { params: params })
            .toPromise()
    }

    tilejson(url: string): Promise<TileJson> {
        let params: HttpParams = new HttpParams();

        return this.http
            .get<TileJson>(url)
            .toPromise()
    }

    getMapboxKey(): string {
        return mapboxKey;
    }

}