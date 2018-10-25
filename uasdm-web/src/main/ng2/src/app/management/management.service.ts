import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams } from '@angular/http';
import 'rxjs/add/operator/toPromise';
import { TreeNode } from 'angular-tree-component';

declare var acp: any;

@Injectable()
export class ManagementService {

  constructor(private http: Http) { }

  getChildren(node:TreeNode): Promise<TreeNode[]> {
    return this.http
      .get(acp + '/project/get-children')
      .toPromise()
      .then(response => {
        return response.json() as TreeNode[];
      })
      .catch((err) => {
        console.error(err);
        
        return [] as TreeNode[];          
      });  
  }  
  
  roots(): Promise<TreeNode[]> {
    return this.http
      .get(acp + '/project/roots')
      .toPromise()
      .then(response => {
        return response.json() as TreeNode[];
      })
      .catch((err) => {
        console.error(err);
        
        return [] as TreeNode[];          
      });  
  }  
}
