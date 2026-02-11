///
///
///

import { Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ErrorHandler } from '@shared/component';

import { ManagementService } from '@site/service/management.service';
import { NgIf } from '@angular/common';
import { SafeHtmlPipe } from '@shared/pipe/safe-html.pipe';


@Component({
    standalone: true,
    selector: 'image-preview-modal',
    templateUrl: './image-preview-modal.component.html',
    styleUrls: ['./image-preview-modal.component.css'],
    imports: [NgIf, SafeHtmlPipe]
})
export class ImagePreviewModalComponent {

    message: string = null;
    open: boolean = true;
    loading: boolean = true;
    imageToShow: any;
    private imageBlob?: Blob;
    productId: string;
    key: string;
    header: string;

    constructor( private service: ManagementService, public bsModalRef: BsModalRef ) { }

    initProduct( productId: string, header: string = null ) {
        this.productId = productId;
        this.header = header;

        this.getProductPreview( this.productId );
    }

    initRaw( productId: string, rawKey: string, header: string = null ) {
        this.productId = productId;
        this.key = rawKey;
        this.header = header;

        this.getRaw(this.productId, this.key);
    }

    createImageFromBlob( image: Blob ) {
        this.imageBlob = image;
        let reader = new FileReader();
        reader.addEventListener( "load", () => {
            this.imageToShow = reader.result;
        }, false );

        if ( image ) {
            reader.readAsDataURL( image );
        }
    }

    getRaw( productId: string, key: string ): void {

        this.loading = true;

        // 0 here is the entire image. Larger number retrieves a smaller image from the cog.
        let imageSize = 2;
        
        this.service.download( productId, key, false ).subscribe( blob => {
            this.createImageFromBlob( blob );
            this.loading = false;
        }, error => {
            this.loading = false;

            this.error( error );
        } );
    }

    getProductPreview( productId: string ): void {

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

    download(filename: string = this.deriveFilename()) {
        if (!this.imageBlob) return;
        const url = URL.createObjectURL(this.imageBlob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename; // e.g. "product-123.jpg"
        document.body.appendChild(a); // Firefox needs it in the DOM
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
    }

    private deriveFilename(): string {
        // Make a simple, safe default filename
        const base = this.key ?? this.productId ?? 'image';
        return `${base}`.replace(/[^\w.-]+/g, '_');
    }

    close(): void {
        this.open = false;
    }

    error(err: HttpErrorResponse): void {
	  this.message = ErrorHandler.getMessageFromError(err);
	}

}
