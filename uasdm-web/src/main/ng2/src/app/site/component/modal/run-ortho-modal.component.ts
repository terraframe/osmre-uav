import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, Input, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ErrorHandler } from '@shared/component';

import { ManagementService } from '@site/service/management.service';
import { SiteEntity } from '@site/model/management';
import { Subject } from 'rxjs';


@Component({
    selector: 'run-ortho-modal',
    templateUrl: './run-ortho-modal.component.html',
    styleUrls: []
})
export class RunOrthoModalComponent implements OnInit, OnDestroy {

    message: string = null;
    entity: SiteEntity = null;

    processPtcloud: boolean = true;
    processDem: boolean = true;
    processOrtho: boolean = true;

    /*
     * Called on confirm
     */
    public onConfirm: Subject<any>;

    constructor(public bsModalRef: BsModalRef) { }

    init(entity: SiteEntity) {
        this.entity = entity;
    }

    ngOnInit(): void {
        this.onConfirm = new Subject();
    }

    ngOnDestroy(): void {
        this.onConfirm.unsubscribe();
    }

    confirm(): void {
        this.onConfirm.next({
            processPtcloud: this.processPtcloud,
            processDem: this.processDem,
            processOrtho: this.processOrtho
        });
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
