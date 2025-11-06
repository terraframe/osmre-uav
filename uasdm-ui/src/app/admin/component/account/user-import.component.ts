///
///
///

import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { FileUploader, FileUploaderOptions } from 'ng2-file-upload';

import { EventService } from '@shared/service/event.service';
import EnvironmentUtil from '@core/utility/environment-util';



@Component( {

    standalone: false,
  selector: 'user-import',
    templateUrl: './user-import.component.html',
    styles: []
} )
export class UserImportComponent implements OnInit {
    message: string = null;

    public uploader: FileUploader;
    public dropActive: boolean = false;

    @ViewChild( 'uploadEl' )
    private uploadElRef: ElementRef;
    
    public onSuccess: Subject<any>;

    file: any;
    context: string;

    constructor(
        private route: ActivatedRoute,
        private location: Location,
        public bsModalRef: BsModalRef,
        private eventService: EventService ) {
        this.context = EnvironmentUtil.getApiUrl();
    }

    ngOnInit(): void {
		
		this.onSuccess = new Subject();

        let options: FileUploaderOptions = {
            autoUpload: true,
            queueLimit: 1,
            removeAfterUpload: true,
            url: EnvironmentUtil.getApiUrl() + '/api/uasdm-account/uploadUsers'
        };

        this.uploader = new FileUploader( options );
        this.uploader.onBeforeUploadItem = ( fileItem: any ) => {
            this.eventService.start();
        };
        this.uploader.onCompleteItem = ( item: any, response: any, status: any, headers: any ) => {
            this.eventService.complete();
        };
        this.uploader.onSuccessItem = ( item: any, response: string, status: number, headers: any ) => {
            this.onSuccess.next(item);
            this.bsModalRef.hide();
        };
        this.uploader.onErrorItem = ( item: any, response: string, status: number, headers: any ) => {
            this.error( response );
        };
        this.uploader.onBuildItemForm = ( fileItem: any, form: any ) => {
        };
    }

    ngAfterViewInit() {
        let that = this;

        this.uploader.onAfterAddingFile = ( item => {
			/*
            this.uploadElRef.nativeElement.value = ''

            let reader = new FileReader();
            reader.onload = function( e: any ) {
                that.file = reader.result;
                that.onSubmit();
            };
            reader.readAsDataURL( item._file );
            */
        } );
    }

    fileOver( e: any ): void {
        this.dropActive = e;
    }

    cancel(): void {
        this.bsModalRef.hide();
    }

    onSubmit(): void {
        if ( this.file == null ) {
            //this.location.back();
            window.location.reload();
        }
        else {
            this.uploader.uploadAll();
        }
    }

    clear(): void {
        this.file = null;

        this.uploader.clearQueue()
    }
// '{"localizedMessage":"Encountered a problem while importing row 1. {message}","developerMessage":"Encountered a problem while importing row 1. {message}","dto_type":"com.runwaysdk.RunwayExceptionDTO","wrappedException":"com.runwaysdk.business.SmartExceptionDTO"}'
    error( err: string ): void {
		if (err == null) { return; }
		
		try
		{
			this.message = JSON.parse(err).localizedMessage;
        }
        catch(err)
        {
			this.message = err;
		}
    }
}
