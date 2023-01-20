import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, Input } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ErrorHandler } from '@shared/component';

import { ManagementService } from '@site/service/management.service';


@Component( {
    selector: 'image-preview-modal',
    templateUrl: './image-preview-modal.component.html',
    styleUrls: []
} )
export class ImagePreviewModalComponent {

    message: string = null;
    open: boolean = true;
    loading: boolean = true;
    imageToShow: any;
    productId: string;

    constructor( private service: ManagementService, public bsModalRef: BsModalRef ) { }

    init( productId: string ) {
        this.productId = productId;

        this.getImage( this.productId );
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

    getImage( productId: string ): void {

        this.loading = true;

        // 0 here is the entire image. Larger number retrieves a smaller image from the cog.
        let imageSize = 2;
        
        this.service.downloadProductPreview( productId, false ).subscribe( blob => {
            this.createImageFromBlob( blob );
            this.loading = false;
        }, error => {
            this.loading = false;

            this.error( error );
        } );
    }

    close(): void {
        this.open = false;
    }

    error(err: HttpErrorResponse): void {
	  this.message = ErrorHandler.getMessageFromError(err);
	}

}
