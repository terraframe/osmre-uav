///
///
///

import { Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ErrorHandler } from '@shared/component';

import { SiteEntity, UserAccess } from '@site/model/management';
import { UserAccessService } from '@site/service/user-access.service';
import { FormsModule } from '@angular/forms';
import { NgFor } from '@angular/common';


@Component({
    standalone: true,
    selector: 'user-access-modal',
    templateUrl: './user-access-modal.component.html',
    styleUrls: [],
    imports: [FormsModule, NgFor]
})
export class UserAccessModalComponent {

    entity: SiteEntity = null;
    identifier: string = null;

    list: UserAccess[] = [];

    constructor(public bsModalRef: BsModalRef, private service: UserAccessService) { }

    init(entity: SiteEntity) {
        this.entity = entity;

        this.service.listUsers(this.entity.id).then(list => {
            this.list = list;
        });
    }

    handleGrantAccess(): void {

        this.service.grantAccess(this.entity.id, this.identifier).then(access => {
            this.list.push(access);
        });
    }

    handleRemoveAccess(access: UserAccess): void {

        this.service.removeAccess(this.entity.id, access.name).then(() => {
            const index = this.list.findIndex(l => l.oid === access.oid);

            if (index !== -1) {
                this.list.splice(index);
            }
        })
    }


}
