import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams } from '@angular/http';

import 'rxjs/add/operator/toPromise';

import { Project } from './management';

declare var acp: any;

@Injectable()
export class ManagementService {

  constructor(private http: Http) { }

  getProjects(): Promise<Project[]> {
    return this.http
      .get(acp + '/project/list')
      .toPromise()
      .then(response => {
        return response.json() as Project[];
      })
      .catch((err) => {
        console.error(err);
        
        return [] as Project[];          
      });  
  }  
}
