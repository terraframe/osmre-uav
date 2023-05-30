///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service'



@Injectable()
export class ForgotPasswordCompleteService {

    constructor( private http: HttpClient, private eventService: EventService ) { }

}
