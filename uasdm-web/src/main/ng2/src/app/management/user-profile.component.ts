import { Component, OnInit, Inject, ViewChild, TemplateRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';

import { MetadataModalComponent } from './modals/metadata-modal.component';
import { ErrorModalComponent } from './modals/error-modal.component';
import { ConfirmModalComponent } from './modals/confirm-modal.component';
import { Message, Task } from '../model/management';
import { ManagementService } from '../service/management.service';

@Component( {
    selector: 'user-profile',
    templateUrl: './user-profile.component.html',
    styleUrls: ['./user-profile.css']
} )
export class UserProfileComponent implements OnInit {

    userName: string = "";
    totalTaskCount: number = 0;
    totalActionsCount: number = 0;

    /*
     * Reference to the modal current showing
     */
    bsModalRef: BsModalRef;

    /*
     * List of messages
     */
    messages: Message[];

    /*
     * List of tasks
     */
    tasks: Task[];

    constructor( private managementService: ManagementService, private modalService: BsModalService, private contextMenuService: ContextMenuService ) { }

    ngOnInit(): void {
        this.userName = this.managementService.getCurrentUser();

        this.managementService.tasks().then( data => {
            this.messages = data.messages;

            this.totalTaskCount = data.tasks.length;

            this.totalActionsCount = this.getTotalActionsCount( data.tasks );

            this.tasks = data.tasks.sort(( a: any, b: any ) =>
                new Date( b.lastUpdatedDate ).getTime() - new Date( a.lastUpdatedDate ).getTime()
            );

        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    getTotalActionsCount( tasks: Task[] ) {
        let count = 0;
        tasks.forEach(( task ) => {
            count = count + task.actions.length;
        } )

        return count
    }

    handleMessage( message: Message ): void {
        this.bsModalRef = this.modalService.show( MetadataModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            'class': 'upload-modal'
        } );
        this.bsModalRef.content.missionId = message.missionId;

        ( <MetadataModalComponent>this.bsModalRef.content ).onMetadataChange.subscribe( () => {
            // Remove the message
            const index = this.messages.indexOf( message );
            

            if ( index > -1 ) {
                this.messages.splice( index, 1 );
            }
        } );

    }

    removeTask(task: Task): void {

        this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = 'Are you sure you want to delete [' + task.label + ']';
        this.bsModalRef.content.data = task;

        ( <ConfirmModalComponent>this.bsModalRef.content ).onConfirm.subscribe( task => {
            this.deleteTask( task );
        } );

    }

    deleteTask(task: Task) {
        this.managementService.removeTask(task.uploadId)
            .then(() => {
                let pos = null;
                for (let i = 0; i < this.tasks.length; i++) {
                    let thisTask = this.tasks[i];

                    if (thisTask.uploadId === task.uploadId) {
                        pos = i;
                        break;
                    }
                }

                if (pos !== null) {
                    this.tasks.splice(pos, 1);
                }

                this.getMissingMetadata();

                this.totalTaskCount = this.tasks.length;

                this.totalActionsCount = this.getTotalActionsCount( this.tasks );

            })
            .catch((err: any) => {
                this.error(err.json());
            });
    }

    getMissingMetadata(): void {

        this.managementService.getMissingMetadata()
            .then( messages => {
                this.messages = messages;
            })
            .catch((err: any) => {
                this.error(err.json());
            });

    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }

    }
}
