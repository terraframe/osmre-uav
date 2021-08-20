import { Component, OnInit, Input } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';

import { PageResult } from '@shared/model/page';
import { Classification, ComponentMetadata } from '@site/model/classification';
import { ClassificationComponent } from './classification.component';

declare let acp: string;

@Component({
    selector: 'classifications',
    templateUrl: './classifications.component.html',
    styles: ['./classifications.css']
})
export class ClassificationsComponent implements OnInit {

    @Input() metadata: ComponentMetadata;

    res: PageResult<Classification> = {
        resultSet: [],
        count: 0,
        pageNumber: 1,
        pageSize: 10
    };
    bsModalRef: BsModalRef;
    message: string = null;

    constructor(private modalService: BsModalService) { }

    ngOnInit(): void {
        this.metadata.service.page(1).then(res => {
            this.res = res;
        });
    }

    remove(classification: Classification): void {
        this.metadata.service.remove(classification.oid).then(response => {
            this.res.resultSet = this.res.resultSet.filter(h => h.oid !== classification.oid);
        });
    }

    onRemove(classification: Classification): void {
        this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        });
        this.bsModalRef.content.message = "Are you sure you want to remove the " + this.metadata.label + " [" + classification.label + "]";
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = "Delete";

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.remove(classification);
        });
    }

    onEdit(classification: Classification): void {
        this.metadata.service.edit(classification.oid).then(res => {
            this.showModal(res, false);
        });
    }

    newInstance(): void {
        this.metadata.service.newInstance().then(res => {
            this.showModal(res, true);
        });
    }

    showModal(classification: Classification, newInstance: boolean): void {
        this.bsModalRef = this.modalService.show(ClassificationComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        });
        this.bsModalRef.content.init(this.metadata, classification, newInstance);

        this.bsModalRef.content.onClassificationChange.subscribe(data => {
            this.onPageChange(this.res.pageNumber);
        });
    }

    onPageChange(pageNumber: number): void {
        this.metadata.service.page(pageNumber).then(res => {
            this.res = res;
        });
    }
}
