///
///
///

import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ErrorHandler } from '@shared/component';

import { SiteEntity, ImageSet } from '@site/model/management';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { ForbiddenNameDirective } from '@site/directive/forbidden-name.directive';
import { ImageSetService } from '@site/service/image-set.service';
import { BooleanFieldComponent } from '@shared/component/boolean-field/boolean-field.component';
import { Observer, Subject } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';


@Component({
    standalone: true,
    selector: 'create-image-set-modal',
    templateUrl: './create-image-set-modal.component.html',
    imports: [FormsModule, NgIf, NgFor, ForbiddenNameDirective, BooleanFieldComponent]
})
export class CreateImageSetModalComponent implements OnInit, OnDestroy {

    message: string = null;

    entity: SiteEntity = null;
    item: SiteEntity = null;
    sets: ImageSet[] = [];

    useExisting: boolean = false;
    newName: string = "";
    name: string = null;
    files: string[] = [];

    onUpdate: Subject<ImageSet>;


    constructor(public bsModalRef: BsModalRef, private service: ImageSetService) { }

    ngOnInit(): void {
        this.onUpdate = new Subject();
    }

    ngOnDestroy(): void {
        this.onUpdate.unsubscribe();
    }

    init(entity: SiteEntity, item: SiteEntity, sets: ImageSet[], observerOrNext?: Partial<Observer<ImageSet>> | ((value: ImageSet) => void)) {
        this.entity = entity;
        this.item = item;
        this.sets = sets;

        this.useExisting = sets.length > 0;

        if (sets.length > 0) {
            this.name = sets[0].name;
        }

        this.onUpdate.subscribe(observerOrNext)
    }

    confirm(): void {
        this.service.create(this.entity.id, !this.useExisting, !this.useExisting ? this.newName : this.name, this.item.id).then((set: ImageSet) => {
            this.onUpdate.next(set)
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
