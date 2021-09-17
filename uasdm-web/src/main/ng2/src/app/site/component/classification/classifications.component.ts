import { Component, OnInit, Input } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';

import { Classification, ClassificationComponentMetadata } from '@site/model/classification';
import { ActivatedRoute, Router } from '@angular/router';
import { ClassificationService } from '@site/service/classification.service';
import { GenericTableColumn, GenericTableConfig, TableEvent } from '@shared/model/generic-table';
import { Subject } from 'rxjs';

declare let acp: string;

@Component({
    selector: 'classifications',
    templateUrl: './classifications.component.html',
    styles: ['./classifications.css']
})
export class ClassificationsComponent implements OnInit {

    _metadata: ClassificationComponentMetadata = {label: "", title: "", baseUrl: ""};
    @Input() set metadata(value: ClassificationComponentMetadata) {
        this._metadata.title = value.title;
        this._metadata.label = value.label;
        this._metadata.baseUrl = value.baseUrl;
        
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
        { header: 'Name', field: 'label', type: 'TEXT', sortable: true },
        { header: '', type: 'ACTIONS', sortable: false },
    ];
    refresh: Subject<void>;

    constructor(private activatedroute: ActivatedRoute, private router: Router, private service: ClassificationService, private modalService: BsModalService) {
    }

    ngOnInit(): void {
//        this.activatedroute.data.subscribe(data => {
//            this.metadata = data as ClassificationComponentMetadata;
//            console.log(data)
//
////            if (this.metadata.columns === undefined) {
////                this.metadata.columns = [];
////            }
////
////            this.config = {
////                service: this.service,
////                remove: true,
////                view: true,
////                create: true,
////                label: this.metadata.label
////            }
//
//            this.refresh = new Subject<void>();
//
//            // this.service.page(this.metadata.baseUrl, 1).then(res => {
//            //     this.res = res;
//            // });
//
//        })
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
            backdrop: true,
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
        this.router.navigate(['/site/' + this._metadata.baseUrl, classification.oid]);
    }

    newInstance(): void {
        this.router.navigate(['/site/' + this._metadata.baseUrl, '__NEW__']);
    }
}
