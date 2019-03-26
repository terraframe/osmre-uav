import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { TreeNode } from 'angular-tree-component';

import { SiteEntity, AttributeType } from '../management';
import { ManagementService } from '../management.service';

@Component( {
    selector: 'edit-modal',
    templateUrl: './edit-modal.component.html',
    styleUrls: []
} )
export class EditModalComponent implements OnInit {

    /*
     * Domain object being updated
     */
    entity: SiteEntity;

    attributes: AttributeType[];

    message: string = null;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onNodeChange: Subject<SiteEntity>;

    constructor( private service: ManagementService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onNodeChange = new Subject();
    }

    handleOnSubmit(): void {
        this.message = null;

        this.service.update( this.entity ).then( node => {
            this.onNodeChange.next( node );

            this.bsModalRef.hide();
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
        }
    }


}
