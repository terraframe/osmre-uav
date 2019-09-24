import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { TreeNode } from 'angular-tree-component';

import { SiteEntity, AttributeType, Condition } from '../../model/management';
import { ManagementService } from '../../service/management.service';


@Component( {
    selector: 'entity-modal',
    templateUrl: './entity-modal.component.html',
    styleUrls: []
} )
export class EntityModalComponent implements OnInit {
    /*
     * parent id of the node being created
     */
    parentId: string;

    entity: SiteEntity;

    attributes: AttributeType[];

    admin: boolean = false;

    newInstance: boolean = false;

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

        if ( this.entity.type !== 'Site' || this.entity.geometry != null ) {
            if ( this.newInstance ) {
                this.service.applyWithParent( this.entity, this.parentId ).then( data => {
                    this.onNodeChange.next( data );
                    this.bsModalRef.hide();
                } ).catch(( err: HttpErrorResponse ) => {
                    this.error( err );
                } );
            }
            else {
                this.service.update( this.entity ).then( node => {
                    this.onNodeChange.next( node );

                    this.bsModalRef.hide();
                } ).catch(( err: HttpErrorResponse ) => {
                    this.error( err );
                } );
            }
        }
        else {
            this.message = "Sites require a location";
        }
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
