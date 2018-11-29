import { Component, OnInit, Inject, ViewChild, TemplateRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';

import { MetadataModalComponent } from './modals/metadata-modal.component';
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

    handleMessage( message: Message ): void {
        this.bsModalRef = this.modalService.show( MetadataModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            'class': 'upload-modal'
        } );
        this.bsModalRef.content.missionId = message.missionId;

        ( <MetadataModalComponent>this.bsModalRef.content ).onMetadataChange.subscribe( missionId => {
            // Remove the message
            
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
