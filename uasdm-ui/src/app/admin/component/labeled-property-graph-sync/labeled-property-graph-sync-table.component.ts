///
///
///

import { Component, OnInit } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';

import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { GenericTableColumn, GenericTableConfig, TableEvent } from '@shared/model/generic-table';
import { LPGSyncService } from '@shared/service/lpg-sync.service';
import { LPGSync } from '@shared/model/lpg';
import { NgIf } from '@angular/common';
import { GenericTableComponent } from '@shared/component/generic-table/generic-table.component';

@Component({
    standalone: true,
    selector: 'labeled-property-graph-sync-table',
    templateUrl: './labeled-property-graph-sync-table.component.html',
    styleUrls: ['./labeled-property-graph-sync-table.css'],
    imports: [NgIf, GenericTableComponent]
})
export class LPGSyncTableComponent implements OnInit {
    bsModalRef: BsModalRef;
    message: string = null;

    config: GenericTableConfig;
    cols: GenericTableColumn[] = [
        { header: 'URL', field: 'url', type: 'TEXT', sortable: true },
        { header: 'Type', field: 'displayLabel', type: 'LOCALIZED', sortable: true },
        { header: 'For Date', field: 'forDate', type: 'DATE', sortable: true },
        { header: 'Version', field: 'versionNumber', type: 'NUMBER', sortable: true },
        { header: '', type: 'ACTIONS', sortable: false },
    ];
    refresh: Subject<void>;

    constructor(private service: LPGSyncService, private router: Router, private modalService: BsModalService) { }

    ngOnInit(): void {
        this.config = {
            service: this.service,
            remove: true,
            view: true,
            create: true,
            label: 'Synchronization Profile',
            sort: { field: 'url', order: 1 },
        }
        this.refresh = new Subject<void>();
    }

    onClick(event: TableEvent): void {
        if (event.type === 'view') {
            this.onView(event.row as LPGSync);
        }
        else if (event.type === 'remove') {
            this.onRemove(event.row as LPGSync);
        }
        else if (event.type === 'create') {
            this.newInstance();
        }
    }

    remove(sync: LPGSync): void {
        this.service.remove(sync.oid).then(response => {
            this.refresh.next();
        });
    }

    onRemove(sync: LPGSync): void {
        this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
            animated: true,
            backdrop: true, class: 'modal-xl',
            ignoreBackdropClick: true,
        });
        this.bsModalRef.content.message = "Are you sure you want to remove the synchronization profile for [" + sync.url + "]";
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = "Delete";

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.remove(sync);
        });
    }

    onView(sync: LPGSync): void {
        this.router.navigate(['/admin/lpg-sync', sync.oid]);
    }

    newInstance(): void {
        this.router.navigate(['/admin/lpg-sync', '__NEW__']);
    }
}
