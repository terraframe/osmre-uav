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
    src: any;
    imageToShow: any;
    @Input() image: any;

    constructor( private service: ManagementService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.getImage(this.image);
    }

    createImageFromBlob(image: Blob) {
        let reader = new FileReader();
        reader.addEventListener("load", () => {
            this.imageToShow = reader.result;
        }, false);

        if (image) {
            reader.readAsDataURL(image);
        }
    }

    getImage(image: any): void {

        this.service.download(image.component, image.key, false).subscribe(blob => {
            this.createImageFromBlob(blob);
        }, error => {
            console.log(error);
        });
    }

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
