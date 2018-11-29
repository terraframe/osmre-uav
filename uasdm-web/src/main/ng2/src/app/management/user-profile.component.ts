import { Component, OnInit, Inject, ViewChild, TemplateRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';

import { CreateModalComponent } from './modals/create-modal.component';
import { EditModalComponent } from './modals/edit-modal.component';
import { ConfirmModalComponent } from './modals/confirm-modal.component';
import { ErrorModalComponent } from './modals/error-modal.component';
import { Message, Task } from './management';
import { ManagementService } from './management.service';

@Component( {
    selector: 'user-profile',
    templateUrl: './user-profile.component.html',
    styleUrls: []
} )
export class UserProfileComponent implements OnInit {

    private userName: string = "";

    /*
     * Reference to the modal current showing
     */
    private bsModalRef: BsModalRef;

    /*
     * List of messages
     */
    private messages: Message[];

    /*
     * List of tasks
     */
    private tasks: Task[];

    constructor( private managementService: ManagementService, private modalService: BsModalService, private contextMenuService: ContextMenuService ) { }

    ngOnInit(): void {
        this.userName = this.managementService.getCurrentUser();

        this.managementService.tasks().then( data => {
            this.messages = data.messages;
            this.tasks = data.tasks;
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }

    }
}
