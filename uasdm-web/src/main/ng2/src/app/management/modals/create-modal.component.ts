import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { TreeNode } from 'angular-tree-component';

import { SiteEntity } from '../management';
import { ManagementService } from '../management.service';


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

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onNodeChange: Subject<SiteEntity>;

    constructor( private service: ManagementService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onNodeChange = new Subject();
    }

    handleOnSubmit(): void {
        this.service.applyWithParent( this.entity, this.parentId ).then( data => {
            this.onNodeChange.next( data );
            this.bsModalRef.hide();
        } );
    }
}
