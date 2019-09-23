import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';

import { ManagementService } from '../../service/management.service';

import { Option } from '../../model/management';


declare var acp: string;

@Component( {
    selector: 'metadata-modal',
    templateUrl: './metadata-modal.component.html',
    styleUrls: []
} )
export class MetadataModalComponent implements OnInit {
    /*
     * collectionId for the metadata
     */
    collectionId: string;

    message: string = null;

    disabled: boolean = false;

    // imageHeight: string;

    // imageWidth: string;

    metaObject: any = {
        collectionId: "",
        // agency:{
        //     name:"Department of Interior",
        //     shortName: "",
        //     fieldCenter: ""
        // },
        pointOfContact: {
            name: "",
            email: ""
        },
        // project: {
        //     name:"",
        //     shortName:"",
        //     description:""
        // },
        // mission: {
        //     name:"",
        //     description:""
        // },
        // collect: {
        //     name:"",
        //     description:""
        // },
        platform: {
            name: "",
            otherName: "Falcon Fixed Wing",
            class: "",
            type: "Fixed Wing",
            serialNumber: "",
            faaIdNumber: ""
        },
        sensor: {
            name: "",
            otherName: "",
            type: "",
            model: "",
            wavelength: "",
            // imageWidth:"",
            // imageHeight:"",
            sensorWidth: "",
            sensorHeight: "",
            pixelSizeWidth: "",
            pixelSizeHeight: ""
        },
        upload: {
            dataType: "raw"
        }
    };

    /*
     * Observable subject called when metadata upload is successful
     */
    public onMetadataChange: Subject<string>;

    sensors: Option[] = [];
    platforms: Option[] = [];

    otherSensorId: string = "";
    otherPlatformId: string = "";

    constructor( public bsModalRef: BsModalRef, private service: ManagementService ) { }

    ngOnInit(): void {
        this.onMetadataChange = new Subject();

        this.service.getMetadataOptions().then(( options ) => {
            this.sensors = options.sensors;
            this.platforms = options.platforms;

            this.sensors.forEach( sensor => {
                if ( sensor.name === 'OTHER' ) {
                    this.otherSensorId = sensor.oid;
                }
            } );

            this.platforms.forEach( platform => {
                if ( platform.name === 'OTHER' ) {
                    this.otherPlatformId = platform.oid;
                }
            } );
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }


    handleSubmit(): void {

        this.metaObject.collectionId = this.collectionId;
        // this.metaObject.imageWidth = this.imageWidth;
        // this.metaObject.imageHeight = this.imageHeight;

        this.service.submitCollectionMetadata( this.metaObject ).then(() => {
            this.bsModalRef.hide();
            this.onMetadataChange.next( this.collectionId );
        } )
            .catch(( err: HttpErrorResponse ) => {
                this.error( err );
            } );
    }

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );

            console.log( this.message );
        }
    }
}
