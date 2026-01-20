///
///
///

import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { Location, NgIf, NgClass } from '@angular/common';

import { FileUploader, FileUploaderOptions, FileUploadModule } from 'ng2-file-upload';

import { EventService } from '@shared/service/event.service';
import EnvironmentUtil from '@core/utility/environment-util';
import { FormsModule } from '@angular/forms';
import { LocalizeComponent } from '../../../shared/component/localize/localize.component';
import { LocalizePipe } from '../../../shared/pipe/localize.pipe';



@Component({
    standalone: true,
    selector: 'system-logo',
    templateUrl: './system-logo.component.html',
    styles: [],
    imports: [FormsModule, NgIf, FileUploadModule, NgClass, LocalizeComponent, LocalizePipe]
})
export class SystemLogoComponent implements OnInit {
    oid: string;
    message: string = null;

    public uploader: FileUploader;
    public dropActive: boolean = false;

    @ViewChild( 'uploadEl' )
    private uploadElRef: ElementRef;
    
    public onSuccess: Subject<any>;

    file: any;
    context: string;

    constructor(
        public bsModalRef: BsModalRef,
        private eventService: EventService ) {
        this.context = EnvironmentUtil.getApiUrl();
    }

    ngOnInit(): void {
		
		this.onSuccess = new Subject();

        let options: FileUploaderOptions = {
            autoUpload: false,
            queueLimit: 1,
            removeAfterUpload: true,
            url: EnvironmentUtil.getApiUrl() + '/api/logo/apply'
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
            form.append( 'oid', this.oid );
        };
    }

    ngAfterViewInit() {
        let that = this;

        this.uploader.onAfterAddingFile = ( item => {
            this.uploadElRef.nativeElement.value = ''

            let reader = new FileReader();
            reader.onload = function( e: any ) {
                that.file = reader.result;
            };
            reader.readAsDataURL( item._file );
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

    error( err: string ): void {
        // Handle error
        if ( err !== null ) {
            this.message = err;
        }
    }
}
