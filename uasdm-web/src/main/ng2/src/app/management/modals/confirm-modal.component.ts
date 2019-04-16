import { Component, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { ModalTypes } from './modal';

@Component( {
    selector: 'confirm-modal',
    templateUrl: './confirm-modal.component.html',
    styleUrls: []
} )
export class ConfirmModalComponent {
    /*
     * Message
     */
    @Input() message: string = 'Are you sure?';

    @Input() data: any;

    @Input() submitText: string = 'Submit';

    @Input() cancelText: string = 'Cancel';

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
