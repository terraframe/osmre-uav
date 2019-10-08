import { Component, OnInit, Inject, ViewChild, TemplateRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';

import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import 'rxjs/Rx';
import { Observable } from 'rxjs/Rx';

import { MetadataModalComponent } from './modal/metadata-modal.component';
import { BasicConfirmModalComponent } from '../../shared/component/modal/basic-confirm-modal.component';
import { Message, Task } from '../model/management';
import { ManagementService } from '../service/management.service';

declare var acp: any;

@Component( {
    selector: 'user-profile',
    templateUrl: './user-profile.component.html',
    styleUrls: ['./user-profile.css']
} )
export class UserProfileComponent implements OnInit {

    userName: string = "";
    totalTaskCount: number = 0;
    totalActionsCount: number = 0;
    taskPolling: any;

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

    constructor( http: HttpClient, private managementService: ManagementService, private modalService: BsModalService, private contextMenuService: ContextMenuService ) {

        this.taskPolling = Observable.interval( 5000 )
            .switchMap(() => http.get<any>( acp + '/project/tasks' ) )
            .subscribe(( data ) => {
                this.updateTaskData( data );
            } );
    }

    ngOnInit(): void {
        this.userName = this.managementService.getCurrentUser();
        this.managementService.tasks().then( data => {

            this.setTaskData( data );

        } );
    }

    ngOnDestroy(): void {

        if ( this.taskPolling ) {
            this.taskPolling.unsubscribe();
        }
    }

    setTaskData( data: any ): void {
        this.messages = data.messages;

        this.totalTaskCount = data.tasks.length;

        this.totalActionsCount = this.getTotalActionsCount( data.tasks );

        this.tasks = data.tasks.sort(( a: any, b: any ) =>
            new Date( b.lastUpdatedDate ).getTime() - new Date( a.lastUpdatedDate ).getTime()
        );
    }

    updateTaskData( data: any ): void {
        this.messages = data.messages;

        this.totalTaskCount = data.tasks.length;

        this.totalActionsCount = this.getTotalActionsCount( data.tasks );

        // Update existing tasks
        for ( let i = 0; i < data.tasks.length; i++ ) {
            let newTask = data.tasks[i];

            for ( let i2 = 0; i2 < this.tasks.length; i2++ ) {
                let existingTask = this.tasks[i2];
                if ( existingTask.oid === newTask.oid ) {
                    if ( existingTask.label !== newTask.label ) {
                        existingTask.label = newTask.label;
                    }
                    if ( existingTask.lastUpdateDate !== newTask.lastUpdateDate ) {
                        existingTask.lastUpdateDate = newTask.lastUpdateDate;
                    }
                    if ( existingTask.lastUpdatedDate !== newTask.lastUpdatedDate ) {
                        existingTask.lastUpdatedDate = newTask.lastUpdatedDate;
                    }
                    if ( existingTask.message !== newTask.message ) {
                        existingTask.message = newTask.message;
                    }
                    if ( existingTask.status !== newTask.status ) {
                        existingTask.status = newTask.status;
                    }
                    if ( existingTask.odmOutput !== newTask.odmOutput ) {
                        existingTask.odmOutput = newTask.odmOutput;
                    }
                }
            }
        }

        // Add new tasks
        let newTasks = data.tasks.filter( o => !this.tasks.find( o2 => o.oid === o2.oid ) );
        if ( newTasks && newTasks.length > 0 ) {
            newTasks.forEach(( tsk ) => {
                this.tasks.unshift( tsk );
            } )
        }
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
        this.bsModalRef.content.init( message.collectionId );

        this.bsModalRef.content.onMetadataChange.subscribe(( collectionId ) => {

            let index = -1;
            for ( let i = 0; i < this.messages.length; i++ ) {
                let msg = this.messages[i];
                if ( msg.collectionId === collectionId ) {
                    index = i;
                }
            }

            if ( index >= 0 ) {
                this.messages.splice( index, 1 );
            }

        } );

    }

    removeTask( task: Task ): void {

        this.bsModalRef = this.modalService.show( BasicConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = 'Are you sure you want to delete [' + task.label + '?';
        this.bsModalRef.content.data = task;
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = 'Delete';

        ( <BasicConfirmModalComponent>this.bsModalRef.content ).onConfirm.subscribe( task => {
            this.deleteTask( task );
        } );

    }

    deleteTask( task: Task ) {
        this.managementService.removeTask( task.uploadId )
            .then(() => {
                let pos = null;
                for ( let i = 0; i < this.tasks.length; i++ ) {
                    let thisTask = this.tasks[i];

                    if ( thisTask.uploadId === task.uploadId ) {
                        pos = i;
                        break;
                    }
                }

                if ( pos !== null ) {
                    this.tasks.splice( pos, 1 );
                }

                this.getMissingMetadata();

                this.totalTaskCount = this.tasks.length;

                this.totalActionsCount = this.getTotalActionsCount( this.tasks );

            } );
    }

    getMissingMetadata(): void {

        this.managementService.getMissingMetadata()
            .then( messages => {
                this.messages = messages;
            } );
    }
}
