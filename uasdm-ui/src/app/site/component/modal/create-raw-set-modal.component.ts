///
///
///

import { Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ErrorHandler } from '@shared/component';

import { SiteEntity, RawSet } from '@site/model/management';
import { RawSetService } from '@site/service/raw-set.service';


@Component({
    standalone: false,
    selector: 'create-raw-set-modal',
    templateUrl: './create-raw-set-modal.component.html',
    styleUrls: []
})
export class CreateRawSetModalComponent {

    message: string = null;

    entity: SiteEntity = null;
    name: string = null;
    files: string[] = [];

    constructor(public bsModalRef: BsModalRef, private service: RawSetService) { }

    init(entity: SiteEntity) {
        this.entity = entity;
    }

    confirm(): void {
        this.service.create(this.entity.id, this.name, this.files).then((rawSet: RawSet) => {
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
