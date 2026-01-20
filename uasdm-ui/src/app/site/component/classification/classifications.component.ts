///
///
///

import { Component, OnInit, Input } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';

import { Classification, ClassificationComponentMetadata } from '@site/model/classification';
import { Router } from '@angular/router';
import { ClassificationService } from '@site/service/classification.service';
import { GenericTableColumn, GenericTableConfig, TableEvent } from '@shared/model/generic-table';
import { Subject } from 'rxjs';
import { NgIf } from '@angular/common';
import { GenericTableComponent } from '../../../shared/component/generic-table/generic-table.component';

@Component({
    standalone: true,
    selector: 'classifications',
    templateUrl: './classifications.component.html',
    styleUrls: ['./classifications.css'],
    imports: [NgIf, GenericTableComponent]
})
export class ClassificationsComponent implements OnInit {

    _metadata: ClassificationComponentMetadata = { label: "", title: "", baseUrl: "", route: "" };

    @Input() set metadata(value: ClassificationComponentMetadata) {
        this._metadata.title = value.title;
        this._metadata.label = value.label;
        this._metadata.baseUrl = value.baseUrl;
        this._metadata.route = value.route;

        this.config = {
            service: this.service,
            remove: true,
            view: true,
            create: true,
            label: value.label
        }
    }

    bsModalRef: BsModalRef;
    message: string = null;

    config: GenericTableConfig;
    cols: GenericTableColumn[] = [
        { header: 'Name', field: 'name', type: 'TEXT', sortable: true },
        { header: '', type: 'ACTIONS', sortable: false },
    ];
    refresh: Subject<void>;

    constructor(private router: Router, private service: ClassificationService, private modalService: BsModalService) {
    }

    ngOnInit(): void {
        this.refresh = new Subject<void>();
    }

    onClick(event: TableEvent): void {
        if (event.type === 'view') {
            this.onView(event.row as Classification);
        }
        else if (event.type === 'remove') {
            this.onRemove(event.row as Classification);
        }
        else if (event.type === 'create') {
            this.newInstance();
        }
    }

    remove(classification: Classification): void {
        this.service.remove(this._metadata.baseUrl, classification.oid).then(response => {
            // this.res.resultSet = this.res.resultSet.filter(h => h.oid !== classification.oid);
            this.refresh.next();
        });
    }

    onRemove(classification: Classification): void {
        this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
            animated: true,
            backdrop: true, class: 'modal-xl',
            ignoreBackdropClick: true,
        });
        this.bsModalRef.content.message = "Are you sure you want to remove the " + this._metadata.label + " [" + classification.name + "]";
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = "Delete";

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.remove(classification);
        });
    }

    onView(classification: Classification): void {
        console.log('Navigate', this._metadata.route)

        this.router.navigate(['/site/' + this._metadata.route, classification.oid]);
    }

    newInstance(): void {
        this.router.navigate(['/site/' + this._metadata.route, '__NEW__']);
    }
}
