import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { CookieService } from 'ngx-cookie-service';

import { SiteEntity, Message, Task } from './management';
import { EventService } from '../event/event.service';

declare var acp: any;

@Injectable()
export class SearchService {

	baseUrl: string = acp + '/project/search';
	
	constructor(private http: Http) { }
	
	search(terms: Observable<string>) {
	  return terms.debounceTime(400)
	    .distinctUntilChanged()
	    .switchMap(term => this.searchEntries(term));
	}
	
	searchEntries(term:string) {
		
      let params: URLSearchParams = new URLSearchParams();
      params.set( 'term', term );

	  return this.http
	      .get(this.baseUrl, { search: params } )
	      .map(res => res.json());
	}

}
