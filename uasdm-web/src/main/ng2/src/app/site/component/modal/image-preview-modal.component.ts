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
    loading: boolean = false;
    imageToShow: any;
    @Input() image: any;

    constructor( private service: ManagementService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        setTimeout(() => {
            this.getImage( this.image );
        }, 0 );
    }

    createImageFromBlob( image: Blob ) {
        let reader = new FileReader();
        reader.addEventListener( "load", () => {
            this.imageToShow = reader.result;
        }, false );

        if ( image ) {
            reader.readAsDataURL( image );
        }
    }

    getImage( image: any ): void {

        this.loading = true;

        this.service.download( image.component, image.key, false ).subscribe( blob => {
            this.createImageFromBlob( blob );
            this.loading = false;
        }, error => {
            this.loading = false;
            console.log( error );
        } );
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
