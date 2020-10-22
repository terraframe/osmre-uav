import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, Input } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ErrorHandler } from '@shared/component';

import { ManagementService } from '../../service/management.service';


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
    component: string;
    key: string;

    constructor( private service: ManagementService, public bsModalRef: BsModalRef ) { }

    init( component: string, key: string ) {
        this.component = component;
        this.key = key;

        this.getImage( this.component, this.key );
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

    getImage( component: string, key: string ): void {

        this.loading = true;

        this.service.download( component, key, false ).subscribe( blob => {
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
