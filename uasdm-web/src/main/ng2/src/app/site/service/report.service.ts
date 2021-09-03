import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpErrorResponse, HttpParams, HttpBackend } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service';
import { HttpBackendClient } from '@shared/service/http-backend-client.service';

import { PageResult } from '@shared/model/page';
import { Report } from '@site/model/report';
import { GenericTableService } from '@site/model/generic-table';

declare var acp: any;

@Injectable()
export class ReportService implements GenericTableService {

    constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService) { }

    page(criteria: Object): Promise<PageResult<Report>> {
        let params: HttpParams = new HttpParams();
        params = params.set('criteria', JSON.stringify(criteria));

        return this.http
            .get<PageResult<Report>>(acp + '/collection-report/page', { params: params })
            .toPromise();
    }
}