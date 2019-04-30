import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';

import { ManagementService } from '../../service/management.service';


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

    metaObject: any = {
        collectionId: "",
        agency:{
            name:"Department of Interior",
            shortName: "",
            fieldCenter: ""
        },
        pointOfContact: {
            name:"",
            email:""
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
            name: "TODO",
            class:"",
            type:"TODO",
            serialNumber:"",
            faaIdNumber:""
        },
        sensor: {
            name:"",
            type:"",
            model:"",
            wavelength:"",
            imageWidth:"",
            imageHeight:"",
            sensorWidth:"",
            sensorHeight:"",
            pixelSizeWidth:"",
            pixelSizeHeight:""
        },
        upload: {
            dataType:"raw"
        }
    };

    /*
     * Observable subject called when metadata upload is successful
     */
    public onMetadataChange: Subject<string>;

    constructor( public bsModalRef: BsModalRef, private service: ManagementService ) { }

    ngOnInit(): void {
        this.onMetadataChange = new Subject();
    }


    handleSubmit(): void {
        
        this.metaObject.collectionId = this.collectionId;

        this.service.submitCollectionMetadata(this.metaObject).then(() => {
            this.bsModalRef.hide();
            this.onMetadataChange.next( this.collectionId );
        } )
        .catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );

            console.log( this.message );
        }
    }
}
