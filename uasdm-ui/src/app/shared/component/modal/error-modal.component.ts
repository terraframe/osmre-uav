///
///
///

import { Component, Inject, Input } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { DOCUMENT } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
    standalone: true,
    selector: 'error-modal',
    templateUrl: './error-modal.component.html',
    styleUrls: ['./error-modal.css'],
    imports: [FormsModule]
})
export class ErrorModalComponent {
    /*
     * Message
     */
    @Input() message: string = 'Unable to complete your action';

    constructor( public bsModalRef: BsModalRef, public modalService: BsModalService, @Inject(DOCUMENT) private document: Document ) { }

    public close(): void {
        this.bsModalRef.hide();

        // If another modal is open, bootstrap can remove this class from the body and screw up scrolling.
        var modal = this.document.querySelector("modal-container.modal");
        if (modal) {
            window.setTimeout(() => {
                this.document.body.classList.add('modal-open');
            },1000); // TODO : There might be a way to do this by listening to the onHidden event from the bsModal but I can't find an easy way to do it.
        }
    }
}
