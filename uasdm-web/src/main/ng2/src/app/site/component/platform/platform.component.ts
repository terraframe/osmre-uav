import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { ErrorHandler } from '@shared/component';

import { Platform } from '@site/model/platform';
import { PlatformService } from '@site/service/platform.service';


@Component( {
    selector: 'platform',
    templateUrl: './platform.component.html',
    styleUrls: []
} )
export class PlatformComponent implements OnInit {
    platform: Platform;
    newInstance: boolean = false;

    message: string = null;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onPlatformChange: Subject<Platform>;

    constructor( private service: PlatformService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onPlatformChange = new Subject();
    }

    handleOnSubmit(): void {
        this.message = null;

        this.service.apply( this.platform ).then( data => {
            this.onPlatformChange.next( data );
            this.bsModalRef.hide();
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    handleOnCancel(): void {
        this.message = null;

        if ( this.newInstance ) {
            this.bsModalRef.hide();
        }
        else {
            this.service.unlock( this.platform.oid ).then( data => {
                this.bsModalRef.hide();
            } ).catch(( err: HttpErrorResponse ) => {
                this.error( err );
            } );
        }
    }

    error(err: HttpErrorResponse): void {
	  this.message = ErrorHandler.getMessageFromError(err);
	}

}
