import { Component, OnInit, Input } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';

import { PageResult } from '@shared/model/page';
import { Classification, ClassificationComponentMetadata } from '@site/model/classification';
import { ClassificationComponent } from './classification.component';
import { ActivatedRoute, Router } from '@angular/router';
import { ClassificationService } from '@site/service/classification.service';

declare let acp: string;

@Component({
    selector: 'classifications',
    templateUrl: './classifications.component.html',
    styles: ['./classifications.css']
})
export class ClassificationsComponent implements OnInit {

    metadata: ClassificationComponentMetadata;

    res: PageResult<Classification> = {
        resultSet: [],
        count: 0,
        pageNumber: 1,
        pageSize: 10
    };
    bsModalRef: BsModalRef;
    message: string = null;

    constructor(private activatedroute: ActivatedRoute, private router: Router, private service: ClassificationService, private modalService: BsModalService) { }

    ngOnInit(): void {
        this.activatedroute.data.subscribe(data => {
            this.metadata = data as ClassificationComponentMetadata;

            if(this.metadata.columns === undefined) {
                this.metadata.columns = [];
            }

            this.service.page(this.metadata.baseUrl, 1).then(res => {
                this.res = res;
            });

        })
    }

    remove(classification: Classification): void {
        this.service.remove(this.metadata.baseUrl, classification.oid).then(response => {
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

    onView(classification: Classification): void {
        this.router.navigate(['/site/' + this.metadata.baseUrl, classification.oid]);
    }

    newInstance(): void {
        this.router.navigate(['/site/' + this.metadata.baseUrl, '__NEW__']);
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
        this.service.page(this.metadata.baseUrl, pageNumber).then(res => {
            this.res = res;
        });
    }
}
