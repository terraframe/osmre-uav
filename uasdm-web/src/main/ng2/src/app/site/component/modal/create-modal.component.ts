import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { TreeNode } from 'angular-tree-component';

import { SiteEntity, AttributeType, Condition } from '../../model/management';
import { ManagementService } from '../../service/management.service';


@Component( {
    selector: 'create-modal',
    templateUrl: './create-modal.component.html',
    styleUrls: []
} )
export class CreateModalComponent implements OnInit {
    /*
     * parent id of the node being created
     */
    parentId: string;

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

        this.service.applyWithParent( this.entity, this.parentId ).then( data => {
            this.onNodeChange.next( data );
            this.bsModalRef.hide();
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    evaluate( condition: Condition ): boolean {
        return this.service.evaluate(condition, this.entity);
    }

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );

            console.log( this.message );
        }
    }

}
