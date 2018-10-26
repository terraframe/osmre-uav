import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { TreeNode } from 'angular-tree-component';

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
    node: TreeNode;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onNodeChange: Subject<TreeNode>;

    constructor( private service: ManagementService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onNodeChange = new Subject();
    }

    handleOnSubmit(): void {
        this.service.update( this.node ).then( node => {
            this.onNodeChange.next( node );
            
            this.bsModalRef.hide();
        } );
    }
}
