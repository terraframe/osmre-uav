///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';

import { SystemLogo } from '../model/system-logo';
import { environment } from 'src/environments/environment';
import { firstValueFrom } from 'rxjs';



@Injectable({ providedIn: 'root' })
export class SystemLogoService {

    constructor(private eventService: EventService, private http: HttpClient) { }

    getIcons(): Promise<SystemLogo[]> {

        this.eventService.start();

        return firstValueFrom(this.http
            .get<{ icons: SystemLogo[] }>(environment.apiUrl + '/api/logo/get-all')
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
        )
            .then(response => {
                return response.icons;
            })
    }

    remove(oid: string): Promise<void> {

        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<void>(environment.apiUrl + '/api/logo/remove', JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
        )
    }
}
