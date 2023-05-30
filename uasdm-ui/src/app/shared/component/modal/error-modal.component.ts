///
///
///

import { Component, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';

@Component( {
    selector: 'error-modal',
    templateUrl: './error-modal.component.html',
    styleUrls: ['./error-modal.css']
} )
export class ErrorModalComponent {
    /*
     * Message
     */
    @Input() message: string = 'Unable to complete your action';

    constructor( public bsModalRef: BsModalRef ) { }
}
