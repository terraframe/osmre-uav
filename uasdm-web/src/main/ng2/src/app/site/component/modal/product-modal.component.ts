import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';

import { NotificationModalComponent } from '../../../shared/component/modal/notification-modal.component';

import { SiteEntity, AttributeType, Condition, ProductDetail, ProductDocument } from '../../model/management';
import { ManagementService } from '../../service/management.service';
import { MetadataService } from '../../service/metadata.service';

declare var acp: string;

@Component( {
    selector: 'product-modal',
    templateUrl: './product-modal.component.html',
    styleUrls: []
} )
export class ProductModalComponent implements OnInit {
    product: ProductDetail;

    thumbnails: any = {};
    items: any[] = [];

    message: string;

    public onGotoSite: Subject<ProductDetail>;

    constructor( private service: ManagementService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onGotoSite = new Subject();
    }

    init( product: ProductDetail ): void {
        this.product = product;

        if ( this.product.imageKey ) {
            this.getThumbnail( this.product.id, this.product.imageKey );
        }

        this.product.documents.forEach( pDocument => {
            this.getThumbnail( pDocument.id, pDocument.key );
        } );
    }

    createImageFromBlob( image: Blob, id: string ) {
        let reader = new FileReader();
        reader.addEventListener( "load", () => {
            // this.imageToShow = reader.result;
            this.thumbnails[id] = reader.result;
        }, false );

        if ( image ) {
            reader.readAsDataURL( image );
        }
    }

    getThumbnail( id: string, key: string ): void {

        const component: string = this.product.entities[this.product.entities.length - 1].id;
        const rootPath: string = key.substr( 0, key.lastIndexOf( "/" ) );
        const fileName: string = /[^/]*$/.exec( key )[0];
        const thumbKey: string = rootPath + "/thumbnails/" + fileName;

        this.service.download( component, thumbKey, false ).subscribe( blob => {
            this.createImageFromBlob( blob, id );
        }, error => {
            console.log( error );
        } );
    }

    getDefaultImgURL( event: any ): void {
        event.target.src = acp + "/net/geoprism/images/thumbnail-default.png";
    }

    handleGoto(): void {
        this.bsModalRef.hide();

        this.onGotoSite.next( this.product );
    }

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
