import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, Input } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';

import { BasicConfirmModalComponent } from '../../../shared/component/modal/basic-confirm-modal.component';

import { SiteEntity, AttributeType, Condition } from '../../model/management';
import { ManagementService } from '../../service/management.service';
import { MetadataService } from '../../service/metadata.service';

import { ImagePreviewModalComponent } from './image-preview-modal.component';
import { FileItem } from 'ng2-file-upload';

import { 
    fadeInOnEnterAnimation, 
    fadeOutOnLeaveAnimation, 
    slideInLeftOnEnterAnimation,
    slideInRightOnEnterAnimation,
 } from 'angular-animations';
import { initDomAdapter } from '@angular/platform-browser/src/browser';

declare var acp: string;

@Component( {
    selector: 'leaf-modal',
    templateUrl: './leaf-modal.component.html',
    styleUrls: [],
    providers: [BasicConfirmModalComponent],
    animations: [ 
        fadeInOnEnterAnimation(),
        fadeOutOnLeaveAnimation(),
        slideInLeftOnEnterAnimation(),
        slideInRightOnEnterAnimation(),
    ]
} )
export class LeafModalComponent implements OnInit {
    entity: SiteEntity;

    @Input() 
    set initData(ins: any){
        this.init(ins.entity, ins.folders, ins.previous)
    }


    /* 
     * Breadcrumb of previous sites clicked on
     */
    previous = [] as SiteEntity[];
    folders: SiteEntity[] = [];
    thumbnails: any = {};
    items: any[] = [];
    processRunning: boolean = false;
    message: string;
    statusMessage: string;
    processable: boolean = false;
    excludes: string[] = [];
    enableSelectableImages: boolean = false;
    folder: SiteEntity;

    /*
     * Reference to the modal current showing
    */
    private confirmModalRef: BsModalRef;


    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onNodeChange: Subject<SiteEntity>;

    constructor( private service: ManagementService, private metadataService: MetadataService
        , private modalService: BsModalService, public bsModalRef: BsModalRef, private basicConfirmModalComponent: BasicConfirmModalComponent ) { }

    ngOnInit(): void {
        this.onNodeChange = new Subject();

        this.excludes = []; // clear excludes if toggling between tabs
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

        if(folder.name === "raw"){
            this.enableSelectableImages = true;
        } else {
            this.enableSelectableImages = false;
        }

        this.service.getItems( folder.component, folder.name ).then( items => {
            //this.images = [items[0]]; // not yet handling different types of files

            // this.images = items;

            this.items = items;

            this.excludes = []; // clear excludes if toggling between tabs

            for ( let i = 0; i < items.length; ++i ) {
                let item = items[i];

                if ( this.isImage( item ) ) {
                    this.getThumbnail( item );
                }

            }
            
            this.folder = folder;
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

        // this.notificationModalRef = this.modalService.show( NotificationModalComponent, {
        //     animated: true,
        //     backdrop: true,
        //     ignoreBackdropClick: true,
        //     class: 'modal-dialog-centered'
        // } );
        // this.notificationModalRef.content.message = "Your ortho task is running for [" + this.entity.name + "]. You can view the current process and results on your tasks page.";
        // this.notificationModalRef.content.submitText = 'OK';


        event.stopPropagation();

        this.confirmModalRef = this.modalService.show( BasicConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            'class': 'confirmation-modal'
        } );
        this.confirmModalRef.content.message = 'Running this process will replace all output products for this ' + this.entity.type + '. Are you sure you want to re-process this data?';
        // this.bsModalRef.content.data = node;
        this.confirmModalRef.content.type = 'DANGER';
        this.confirmModalRef.content.submitText = "Run Process";

        ( <BasicConfirmModalComponent>this.confirmModalRef.content ).onConfirm.subscribe( data => {
            this.processRunning = true;

            this.service.runOrtho( this.entity.id, this.excludes ).then( data => {
                this.processRunning = false;
                this.statusMessage = "Your process is started.";
            } );
        } );

    }

    handleDownload( ): void {

        window.location.href = acp + '/project/download-all?id=' + this.folder.component + "&key=" + this.folder.name;

        //      this.service.downloadAll( data.id ).then( data => {
        //        
        //      } ).catch(( err: HttpErrorResponse ) => {
        //          this.error( err );
        //      } );
    }




    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
