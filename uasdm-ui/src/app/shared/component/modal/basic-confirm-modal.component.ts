///
///
///

import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { ModalTypes } from '../../model/modal';
import { NgIf, NgClass } from '@angular/common';

@Component({
    standalone: true,
    selector: 'basic-confirm-modal',
    templateUrl: './basic-confirm-modal.component.html',
    styleUrls: [],
    imports: [NgIf, NgClass]
})
export class BasicConfirmModalComponent implements OnInit, OnDestroy {
    /*
     * Message
     */
    @Input() message: string = 'Are you sure?';

    @Input() subText: string = null;

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

    ngOnDestroy(): void {
        this.onConfirm.unsubscribe();
    }

    confirm(): void {
        this.bsModalRef.hide();
        this.onConfirm.next( this.data );
    }
}
