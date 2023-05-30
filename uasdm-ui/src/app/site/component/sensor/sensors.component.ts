///
///
///

import { Component, OnInit } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';

import { Sensor } from '@site/model/sensor';
import { SensorService } from '@site/service/sensor.service';
import { Router } from '@angular/router';
import { GenericTableColumn, GenericTableConfig, TableEvent } from '@shared/model/generic-table';
import { Subject } from 'rxjs';

@Component({
    selector: 'sensors',
    templateUrl: './sensors.component.html',
    styleUrls: ['./sensors.css']
})
export class SensorsComponent implements OnInit {
    bsModalRef: BsModalRef;
    message: string = null;

    config: GenericTableConfig;
    cols: GenericTableColumn[] = [
        { header: 'Name', field: 'name', type: 'TEXT', sortable: true },
        { header: 'Model', field: 'model', type: 'TEXT', sortable: true },
        { header: 'Description', field: 'description', type: 'TEXT', sortable: true },
        { header: 'Type', field: 'sensorType', type: 'TEXT', sortable: true },
        { header: '', type: 'ACTIONS', sortable: false },
    ];
    refresh: Subject<void>;


    constructor(private service: SensorService, private modalService: BsModalService, private router: Router) { }


    ngOnInit(): void {

        this.config = {
            service: this.service,
            remove: true,
            view: true,
            create: true,
            label: 'Sensor',
            sort: { field: 'name', order: 1 },
        }

        this.refresh = new Subject<void>();
    }

    onClick(event: TableEvent): void {
        if (event.type === 'view') {
            this.onView(event.row as Sensor);
        }
        else if (event.type === 'remove') {
            this.onRemove(event.row as Sensor);
        }
        else if (event.type === 'create') {
            this.newInstance();
        }
    }


    remove(sensor: Sensor): void {
        this.service.remove(sensor.oid).then(response => {
            this.refresh.next();
        });
    }

    onRemove(sensor: Sensor): void {
        this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        });
        this.bsModalRef.content.message = "Are you sure you want to remove the sensor [" + sensor.name + "]";
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = "Delete";

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.remove(sensor);
        });
    }

    onView(sensor: Sensor): void {
        this.router.navigate(['/site/sensor', sensor.oid]);
    }

    newInstance(): void {
        this.router.navigate(['/site/sensor', '__NEW__']);
    }
}
