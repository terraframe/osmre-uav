///
///
///

import { Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ErrorHandler } from '@shared/component';

import { SiteEntity, ImageSet } from '@site/model/management';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { ForbiddenNameDirective } from '@site/directive/forbidden-name.directive';
import { ImageSetService } from '@site/service/image-set.service';


@Component({
    standalone: true,
    selector: 'create-image-set-modal',
    templateUrl: './create-image-set-modal.component.html',
    imports: [FormsModule, NgIf, ForbiddenNameDirective]
})
export class CreateImageSetModalComponent {

    message: string = null;

    entity: SiteEntity = null;
    name: string = null;
    files: string[] = [];

    constructor(public bsModalRef: BsModalRef, private service: ImageSetService) { }

    init(entity: SiteEntity) {
        this.entity = entity;
    }

    confirm(): void {
        this.service.create(this.entity.id, this.name, this.files).then((rawSet: ImageSet) => {
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
