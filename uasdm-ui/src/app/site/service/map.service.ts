///
///
///

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom, Observable } from 'rxjs';
import { isMapboxURL, transformMapboxUrl } from 'maplibregl-mapbox-request-transformer'

import { map } from 'rxjs/operators';
// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';


import { environment } from 'src/environments/environment';
import { ConfigurationService } from '@core/service/configuration.service';
import { GeoJSONSource } from 'maplibre-gl';



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

    constructor(private http: HttpClient, private configuration: ConfigurationService) {
    }

    features(conditions: { hierarchy: any, array: { field: string, value: any }[] }): Promise<{ features: GeoJSONSource, bbox: number[] }> {
        let params: HttpParams = new HttpParams();
        if (conditions != null) {
            params = params.set('conditions', JSON.stringify(conditions));
        }

        return this.http
            .get<{ features: GeoJSONSource, bbox: number[] }>(environment.apiUrl + '/api/project/features', { params: params })
            .toPromise()
    }

    mbForwardGeocode(searchText: string): Promise<any> {
        let params: HttpParams = new HttpParams();

        let url = "https://api.mapbox.com/geocoding/v5/mapbox.places/" + searchText + ".json?proximity=-74.70850,40.78375&access_token=" + this.configuration.getMapboxKey();

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
}