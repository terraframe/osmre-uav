///
///
///

import { Component, OnInit } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';

import { Platform } from '@site/model/platform';
import { PlatformService } from '@site/service/platform.service';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { GenericTableColumn, GenericTableConfig, TableEvent } from '@shared/model/generic-table';
import { NgIf } from '@angular/common';
import { GenericTableComponent } from '../../../shared/component/generic-table/generic-table.component';

@Component({
    standalone: true,
    selector: 'platforms',
    templateUrl: './platforms.component.html',
    styleUrls: ['./platforms.css'],
    imports: [NgIf, GenericTableComponent]
})
export class PlatformsComponent implements OnInit {
    bsModalRef: BsModalRef;
    message: string = null;

    config: GenericTableConfig;
    cols: GenericTableColumn[] = [
        { header: 'Name', field: 'name', type: 'TEXT', sortable: true },
        { header: 'Description', field: 'description', type: 'TEXT', sortable: true },
        { header: '', type: 'ACTIONS', sortable: false },
    ];
    refresh: Subject<void>;

    constructor(private service: PlatformService, private router: Router, private modalService: BsModalService) { }

    ngOnInit(): void {
        this.config = {
            service: this.service,
            remove: true,
            view: true,
            create: true,
            label: 'Platform',
            sort: {field: 'name', order: 1}
        }

        this.refresh = new Subject<void>();
    }

    onClick(event: TableEvent): void {
        if (event.type === 'view') {
            this.onView(event.row as Platform);
        }
        else if (event.type === 'remove') {
            this.onRemove(event.row as Platform);
        }
        else if (event.type === 'create') {
            this.newInstance();
        }
    }

    remove(platform: Platform): void {
        this.service.remove(platform.oid).then(response => {
            this.refresh.next();
        });
    }

    onRemove(platform: Platform): void {
        this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
            animated: true,
            backdrop: true, class: 'modal-xl',
            ignoreBackdropClick: true,
        });
        this.bsModalRef.content.message = "Are you sure you want to remove the platform [" + platform.name + "]";
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = "Delete";

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.remove(platform);
        });
    }

    onView(platform: Platform): void {
        this.router.navigate(['/site/platform', platform.oid]);
    }

    newInstance(): void {
        this.router.navigate(['/site/platform', '__NEW__']);
    }
}
