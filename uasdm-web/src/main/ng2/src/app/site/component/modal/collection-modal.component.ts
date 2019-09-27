import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { TabDirective } from 'ngx-bootstrap/tabs';
import { Subject } from 'rxjs/Subject';

import { SiteEntity, AttributeType, Condition } from '../../model/management';
import { ManagementService } from '../../service/management.service';

import { ImagePreviewModalComponent } from './image-preview-modal.component';

declare var acp: string;

@Component( {
    selector: 'collection-modal',
    templateUrl: './collection-modal.component.html',
    styleUrls: []
} )
export class CollectionModalComponent implements OnInit {
    entity: SiteEntity;

    folders: SiteEntity[] = [];

    thumbnails: any = {};
    images: any[] = [];

    message: string;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onNodeChange: Subject<SiteEntity>;

    constructor( private service: ManagementService ) { }

    ngOnInit(): void {
        this.onNodeChange = new Subject();
    }

    init( entity: SiteEntity, folders: SiteEntity[] ): void {
        this.entity = entity;
        this.folders = folders;

        if ( this.folders.length > 0 ) {
            this.onSelect( this.folders[0] );
        }
    }

    createImageFromBlob( image: Blob, imageData: any ) {
        let reader = new FileReader();
        reader.addEventListener( "load", () => {
            // this.imageToShow = reader.result;
            this.thumbnails[imageData.key] = reader.result;
        }, false );

        if ( image ) {
            reader.readAsDataURL( image );
        }
    }

    getThumbnail( image: any ): void {

        let rootPath: string = image.key.substr( 0, image.key.lastIndexOf( "/" ) );
        let fileName: string = /[^/]*$/.exec( image.key )[0];
        let thumbKey: string = rootPath + "/thumbnails/" + fileName;

        this.service.download( image.component, thumbKey, false ).subscribe( blob => {
            this.createImageFromBlob( blob, image );
        }, error => {
            console.log( error );
        } );
    }

    onSelect( folder: SiteEntity ): void {
        console.log( "Setting folder active: " + folder.name );

        // clear any existing images
        this.images = [];

        this.service.getItems( folder.component, folder.name ).then( items => {
            //this.images = [items[0]]; // not yet handling different types of files

            // this.images = items;

            for ( let i = 0; i < items.length; ++i ) {
                let item = items[i];

                if ( item.name.toLowerCase().indexOf( ".png" ) !== -1 || item.name.toLowerCase().indexOf( ".jpg" ) !== -1 ||
                    item.name.toLowerCase().indexOf( ".jpeg" ) !== -1 || item.name.toLowerCase().indexOf( ".tif" ) !== -1 ||
                    item.name.toLowerCase().indexOf( ".tiff" ) !== -1 ) {

                    this.images.push( item );
                }
            }

            this.images.forEach( image => {
                this.getThumbnail( image );
            } )
        } );
    }

    getDefaultImgURL( event: any ): void {
        event.target.src = acp + "/net/geoprism/images/thumbnail-default.png";
    }

    previewImage( event: any, image: any ): void {
        //        this.bsModalRef = this.modalService.show( ImagePreviewModalComponent, {
        //            animated: true,
        //            backdrop: true,
        //            ignoreBackdropClick: true,
        //            'class': 'image-preview-modal'
        //        } );
        //        this.bsModalRef.content.image = image;
        //        this.bsModalRef.content.src = event.target.src;
    }



    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
