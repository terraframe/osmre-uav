import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { TreeNode } from 'angular-tree-component';

import { SiteEntity } from '../management';
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

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onNodeChange: Subject<SiteEntity>;

    constructor( private service: ManagementService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onNodeChange = new Subject();
    }

    handleOnSubmit(): void {
        this.service.update( this.entity ).then( node => {
            this.onNodeChange.next( node );
            
            this.bsModalRef.hide();
        } );
    }
}
