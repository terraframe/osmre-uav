///
///
///

import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { ModalTypes } from '../../model/modal';

@Component( {
    standalone: false,
  selector: 'notification-modal',
    templateUrl: './notification-modal.component.html',
    styleUrls: []
} )
export class NotificationModalComponent implements OnInit, OnDestroy {
    /*
     * Message
     */
    @Input() message: string = '';
    
    @Input() messageTitle: string = '';

    @Input() data: any;

    @Input() submitText: string = 'Submit';

    @Input() type: ModalTypes = ModalTypes.warning;

    /*
     * Called on confirm
     */
    public onConfirm: Subject<any>;

    constructor( public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onConfirm = new Subject();
    }

    ngOnDestroy(): void {
        this.onConfirm.unsubscribe();
    }

    confirm(): void {
        this.bsModalRef.hide();
        this.onConfirm.next( this.data );
    }
}
