import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';

import { NotificationModalComponent } from '../../../shared/component/modal/notification-modal.component';

import { SiteEntity, AttributeType, Condition } from '../../model/management';
import { ManagementService } from '../../service/management.service';
import { MetadataService } from '../../service/metadata.service';

import { ImagePreviewModalComponent } from './image-preview-modal.component';
import { FileItem } from 'ng2-file-upload';

declare var acp: string;

@Component( {
    selector: 'leaf-modal',
    templateUrl: './leaf-modal.component.html',
    styleUrls: []
} )
export class LeafModalComponent implements OnInit {
    entity: SiteEntity;

    /* 
     * Breadcrumb of previous sites clicked on
     */
    previous = [] as SiteEntity[];

    folders: SiteEntity[] = [];

    thumbnails: any = {};
    items: any[] = [];

    message: string;

    processable: boolean = false;

    excludes: string[] = [];

    /*
     * Reference to the modal current showing
    */
    private notificationModalRef: BsModalRef;


    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onNodeChange: Subject<SiteEntity>;

    constructor( private service: ManagementService, private metadataService: MetadataService
        , private modalService: BsModalService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onNodeChange = new Subject();
    }

    init( entity: SiteEntity, folders: SiteEntity[], previous: SiteEntity[] ): void {
        this.entity = entity;
        this.folders = folders;
        this.previous = [...previous];

        if ( this.previous.length > 0 && this.previous[this.previous.length - 1].id !== this.entity.id ) {
            this.previous.push( this.entity );
        }

        if ( this.folders.length > 0 ) {
            this.onSelect( this.folders[0] );
        }

        this.processable = this.metadataService.isProcessable( entity.type );
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
        // clear any existing items
        this.items = [];

        this.service.getItems( folder.component, folder.name ).then( items => {
            //this.images = [items[0]]; // not yet handling different types of files

            // this.images = items;

            this.items = items;

            for ( let i = 0; i < items.length; ++i ) {
                let item = items[i];

                if ( this.isImage( item ) ) {
                    this.getThumbnail( item );
                }

            }
        } );
    }

    isImage( item: any ): boolean {
        if ( item.name.toLowerCase().indexOf( ".png" ) !== -1 || item.name.toLowerCase().indexOf( ".jpg" ) !== -1 ||
            item.name.toLowerCase().indexOf( ".jpeg" ) !== -1 || item.name.toLowerCase().indexOf( ".tif" ) !== -1 ||
            item.name.toLowerCase().indexOf( ".tiff" ) !== -1 ) {

            return true;
        }
        return false;
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

    toggleExcludeImage( event: any, image: any ): void {
        image.excludeFromProcess = !image.excludeFromProcess;

        if(image.excludeFromProcess){
            this.excludes.push(image.name);
        }
        else {
            let position = this.excludes.indexOf(image.name);
            if(position > -1){
                this.excludes.splice(position, 1);
            }
        }
    }

    isProcessable(item: any): boolean {
        return this.metadataService.isProcessable( item.type );
    }

    handleRunOrtho(): void {

        this.notificationModalRef = this.modalService.show( NotificationModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            class: 'modal-dialog-centered'
        } );
        this.notificationModalRef.content.message = "Your ortho task is running for [" + this.entity.name + "]. You can view the current process and results on your tasks page.";
        this.notificationModalRef.content.submitText = 'OK';

        this.service.runOrtho( this.entity.id, this.excludes ).then( data => {
            // Nothing
        } );
    }



    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
