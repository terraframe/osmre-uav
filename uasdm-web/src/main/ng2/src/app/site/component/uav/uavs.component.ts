import { Component, OnInit } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';

import { UAV } from '@site/model/uav';
import { UAVService } from '@site/service/uav.service';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { GenericTableConfig, TableEvent } from '@site/model/generic-table';

@Component({
    selector: 'uavs',
    templateUrl: './uavs.component.html',
    styles: ['./uavs.css']
})
export class UAVsComponent implements OnInit {
    bsModalRef: BsModalRef;
    message: string = null;

    config: GenericTableConfig;
    cols: any = [
        { header: 'Serial Number', field: 'serialNumber', type: 'TEXT', sortable: true },
        { header: 'FAA Id Number', field: 'faaNumber', type: 'TEXT', sortable: true },
        { header: 'Description', field: 'description', type: 'TEXT', sortable: true },
        { header: '', type: 'ACTIONS', sortable: false },
    ];
    refresh: Subject<void>;

    constructor(private service: UAVService, private router: Router, private modalService: BsModalService) { }

    ngOnInit(): void {
        this.config = {
            service: this.service,
            remove: true,
            view: true,
            create: true,
            label: 'UAV'
        }

        this.refresh = new Subject<void>();
    }

    onClick(event: TableEvent): void {
        if (event.type === 'view') {
            this.onView(event.row as UAV);
        }
        else if (event.type === 'remove') {
            this.onRemove(event.row as UAV);
        }
        else if (event.type === 'create') {
            this.newInstance();
        }
    }

    remove(uav: UAV): void {
        this.refresh.next();
    }

    onRemove(uav: UAV): void {
        this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        });
        this.bsModalRef.content.message = "Are you sure you want to remove the UAV [" + uav.serialNumber + "]";
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = "Delete";

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.remove(uav);
        });
    }

    onView(uav: UAV): void {
        this.router.navigate(['/site/uav', uav.oid]);
    }

    newInstance(): void {
        this.router.navigate(['/site/uav', '__NEW__']);
    }
}
