import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ManagementService } from '../../service/management.service';


@Component( {
    selector: 'image-preview-modal',
    templateUrl: './image-preview-modal.component.html',
    styleUrls: []
} )
export class ImagePreviewModalComponent implements OnInit {

    message: string = null;
    open: boolean = true;
    @Input() image: any;
    @Input() src: string = "";

    constructor( private service: ManagementService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {}

    close(): void {
        this.open = false;
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );

            console.log( this.message );
        }
    }

}
