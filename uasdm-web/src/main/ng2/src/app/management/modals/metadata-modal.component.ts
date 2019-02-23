import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';

//use Fine Uploader UI for traditional endpoints
import { FineUploader, UIOptions } from 'fine-uploader';

declare var acp: string;

@Component( {
    selector: 'metadata-modal',
    templateUrl: './metadata-modal.component.html',
    styleUrls: []
} )
export class MetadataModalComponent implements OnInit {
    /*
     * missionId for the metadata
     */
    missionId: string;

    message: string = null;

    disabled: boolean = false;

    /*
     * Observable subject called when metadata upload is successful
     */
    public onMetadataChange: Subject<string>;


    /*
     * FineUploader for uploading large files
     */
    uploader = null as FineUploader;

    constructor( public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onMetadataChange = new Subject();
    }

    @ViewChild( 'uploader' ) set content( elem: ElementRef ) {

        const that = this;

        if ( elem != null && this.uploader == null ) {

            let uiOptions: UIOptions = {
                debug: false,
                autoUpload: false,
                multiple: false,
                element: elem.nativeElement,
                template: 'qq-template',
                request: {
                    endpoint: acp + "/file/metadata",
                    forceMultipart: true
                },
                retry: {
                    enableAuto: false
                },
                validation: {
                    allowedExtensions: ['xml']
                },
                callbacks: {
                    onUpload: function( id: any, name: any ): void {
                        that.disabled = true;
                    },
                    onComplete: function( id: any, name: any, responseJSON: any, xhrOrXdr: any ): void {
                        that.disabled = false;

                        if ( responseJSON.success ) {
                            that.onMetadataChange.next();
                            that.bsModalRef.hide()                            
                        }
                    },
                    onError: function( id: any, name: any, errorReason: any, xhrOrXdr: any ): void {
                        that.disabled = false;
                        that.message = ( errorReason );
                    }

                }

            };

            this.uploader = new FineUploader( uiOptions );
        }
    }

    handleSubmit(): void {
        this.uploader.setParams( { missionId: this.missionId } );
        this.uploader.uploadStoredFiles();
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );

            console.log( this.message );
        }
    }
}
