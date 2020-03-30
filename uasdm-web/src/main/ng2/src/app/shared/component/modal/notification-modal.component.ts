import { Component, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { ModalTypes } from '../../model/modal';

@Component( {
    selector: 'notification-modal',
    templateUrl: './notification-modal.component.html',
    styleUrls: []
} )
export class NotificationModalComponent {
    /*
     * Message
     */
    @Input() message: string = '';

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

    confirm(): void {
        this.bsModalRef.hide();
        this.onConfirm.next( this.data );
    }
}
