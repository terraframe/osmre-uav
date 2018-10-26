import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams } from '@angular/http';
import 'rxjs/add/operator/toPromise';
import { TreeNode } from 'angular-tree-component';

declare var acp: any;

@Injectable()
export class ManagementService {

    constructor( private http: Http ) { }

    getChildren( node: TreeNode ): Promise<TreeNode[]> {
        return this.http
            .get( acp + '/project/get-children' )
            .toPromise()
            .then( response => {
                return response.json() as TreeNode[];
            } )
    }

    roots(): Promise<TreeNode[]> {
        return this.http
            .get( acp + '/project/roots' )
            .toPromise()
            .then( response => {
                return response.json() as TreeNode[];
            } )
    }

    edit( id: string ): Promise<TreeNode> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );


        return this.http
            .post( acp + '/project/edit', JSON.stringify( { id: id } ), { headers: headers } )
            .toPromise()
            .then( response => {
                return response.json() as TreeNode;
            } )
    }

    update( node: TreeNode ): Promise<TreeNode> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );


        return this.http
            .post( acp + '/project/update', JSON.stringify( { node: node } ), { headers: headers } )
            .toPromise()
            .then( response => {
                return response.json() as TreeNode;
            } )
    }

    newChild( id: string ): Promise<TreeNode> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );


        return this.http
            .post( acp + '/project/new-child', JSON.stringify( { id: id } ), { headers: headers } )
            .toPromise()
            .then( response => {
                return response.json() as TreeNode;
            } )
    }



    applyWithParent( node: TreeNode, parentId: string ): Promise<TreeNode> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );


        return this.http
            .post( acp + '/project/apply-with-parent', JSON.stringify( { node: node, parentId: parentId } ), { headers: headers } )
            .toPromise()
            .then( response => {
                return response.json() as TreeNode;
            } )
    }

    remove( id: string ): Promise<Response> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        return this.http
            .post( acp + '/project/remove', JSON.stringify( { id: id } ), { headers: headers } )
            .toPromise()
    }
}
