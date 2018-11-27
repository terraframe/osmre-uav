import { Component, OnInit, AfterViewInit, Inject, ViewChild, ElementRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

//use Fine Uploader UI for traditional endpoints
import { FineUploader, UIOptions } from 'fine-uploader';

import { ErrorModalComponent } from './modals/error-modal.component';
import { SiteEntity, UploadForm } from './management';
import { ManagementService } from './management.service';

declare var acp: string;

@Component( {
    selector: 'upload',
    templateUrl: './upload.component.html',
    styleUrls: []
} )
export class UploadComponent implements OnInit {

    /*
     * Reference to the modal current showing
     */
    private bsModalRef: BsModalRef;

    /* 
     * List of sites
     */
    sites = [] as SiteEntity[];

    /* 
     * List of projects
     */
    projects = [] as SiteEntity[];

    /* 
     * List of missions
     */
    missions = [] as SiteEntity[];

    /* 
     * List of collections
     */
    collections = [] as SiteEntity[];

    /* 
     * Form values
     */
    values = { create: false } as UploadForm;

    /*
     * FineUploader for uploading large files
     */
    uploader = null as FineUploader;

    constructor( private service: ManagementService, private modalService: BsModalService ) { }

    @ViewChild( 'uploader' ) set content( elem: ElementRef ) {

        if ( elem != null && this.uploader == null ) {

            let uiOptions: UIOptions = {
                debug: true,
                autoUpload: false,
                element: elem.nativeElement,
                template: 'qq-template',
                request: {
                    endpoint: acp + "/file/upload"
                },
                resume: {
                    enabled: true
                },
                chunking: {
                    enabled: true
                },
                retry: {
                    enableAuto: true
                },
                text: {
                    defaultResponseError: "Upload failed"
                },
                failedUploadTextDisplay: {
                    mode: 'custom',
                    responseProperty: 'error'
                },
                validation: {
                    allowedExtensions: ['zip', '.tar.gz']
                }

            };

            this.uploader = new FineUploader( uiOptions );
        }
    }

    ngOnInit(): void {
        this.service.roots().then( sites => {
            this.sites = sites;

        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    onSiteSelect( siteId: string ): void {
        this.values.site = siteId;

        if ( siteId != null && siteId.length > 0 ) {

            // Reset select options
            this.projects = [] as SiteEntity[];
            this.missions = [] as SiteEntity[];
            this.collections = [] as SiteEntity[];

            // Reset form values
            this.values.project = null;
            this.values.mission = null;
            this.values.collection = null;

            this.service.getChildren( this.values.site ).then( projects => {
                this.projects = projects;
            } ).catch(( err: any ) => {
                this.error( err.json() );
            } );
        }

        console.log( this.values );
    }

    onProjectSelect( projectId: string ): void {
        this.values.project = projectId;

        // Reset select options
        this.missions = [] as SiteEntity[];
        this.collections = [] as SiteEntity[];

        // Reset form values
        this.values.mission = null;
        this.values.collection = null;

        if ( projectId != null && projectId.length > 0 ) {
            this.service.getChildren( this.values.project ).then( missions => {
                this.missions = missions;
            } ).catch(( err: any ) => {
                this.error( err.json() );
            } );
        }
    }

    onMissionSelect( missionId: string ): void {
        this.values.mission = missionId;

        // Reset select options
        this.collections = [] as SiteEntity[];

        // Reset form values
        this.values.collection = null;

        if ( missionId != null && missionId.length > 0 ) {

            this.service.getChildren( this.values.mission ).then( collections => {
                this.collections = collections;
            } ).catch(( err: any ) => {
                this.error( err.json() );
            } );
        }
    }

    onCollectionSelect( collectionId: string ): void {
        this.values.collection = collectionId;
    }

    handleUpload(): void {

        /*
         * Validate form values before uploading
         */
        if ( !this.values.create && this.values.collection == null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = "A collection must first be selected before the file can be uploaded";
        }
        else if ( this.values.create && ( this.values.mission == null || this.values.name == null || this.values.name.length == 0 ) ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = "Name is required";
        }
        else {
            this.uploader.setParams( this.values );
            this.uploader.uploadStoredFiles();
        }

    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }
    }
}
