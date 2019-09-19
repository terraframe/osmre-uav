import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { TreeNode } from 'angular-tree-component';

import { SiteEntity, AttributeType, Condition } from '../../model/management';
import { ManagementService } from '../../service/management.service';

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

    userName: string = "";

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onNodeChange: Subject<SiteEntity>;

    constructor( private service: ManagementService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onNodeChange = new Subject();

        this.userName = this.service.getCurrentUser();
    }

    handleOnSubmit(): void {
        this.message = null;

        this.service.update( this.entity ).then( node => {
            this.onNodeChange.next( node );

            this.bsModalRef.hide();
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    evaluate( condition: Condition ): boolean {
        return this.service.evaluate( condition, this.entity );
    }

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }


}
