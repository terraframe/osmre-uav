import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';

import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';
import { HttpBackendClient } from '@shared/service/http-backend-client.service';

import { PageResult } from '@shared/model/page';
import { Classification, ClassificationService } from '@site/model/classification';
import { Injectable } from '@angular/core';

declare var acp: any;

export class AbstractClassificationService implements ClassificationService {

    baseUrl: string;

    constructor(baseUrl: string, private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService) {
        this.baseUrl = baseUrl;
    }

    page(p: number): Promise<PageResult<Classification>> {
        let params: HttpParams = new HttpParams();
        params = params.set('number', p.toString());

        this.eventService.start();

        return this.http
            .get<PageResult<Classification>>(this.baseUrl + '/page', { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getAll(): Promise<Classification[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return this.http
            .get<Classification[]>(this.baseUrl + '/get-all', { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }


    edit(oid: string): Promise<Classification> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<Classification>(this.baseUrl + '/get', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    newInstance(): Promise<Classification> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.http
            .post<Classification>(this.baseUrl + '/newInstance', JSON.stringify({}), { headers: headers })
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
            .post<void>(this.baseUrl + '/remove', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    apply(classification: Classification): Promise<Classification> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return this.noErrorHttpClient
            .post<Classification>(this.baseUrl + '/apply', JSON.stringify({ classification: classification }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }
}


@Injectable()
export class PlatformManufacturerService extends AbstractClassificationService implements ClassificationService {

    constructor(http: HttpClient, noErrorHttpClient: HttpBackendClient, eventService: EventService) {
        super(acp + '/platform-manufacturer', http, noErrorHttpClient, eventService);
    }

}

@Injectable()
export class PlatformTypeService extends AbstractClassificationService implements ClassificationService {

    constructor(http: HttpClient, noErrorHttpClient: HttpBackendClient, eventService: EventService) {
        super(acp + '/platform-type', http, noErrorHttpClient, eventService);
    }

}

@Injectable()
export class SensorTypeService extends AbstractClassificationService implements ClassificationService {

    constructor(http: HttpClient, noErrorHttpClient: HttpBackendClient, eventService: EventService) {
        super(acp + '/sensor-type', http, noErrorHttpClient, eventService);
    }

}

@Injectable()
export class WaveLengthService extends AbstractClassificationService implements ClassificationService {

    constructor(http: HttpClient, noErrorHttpClient: HttpBackendClient, eventService: EventService) {
        super(acp + '/wave-length', http, noErrorHttpClient, eventService);
    }

}