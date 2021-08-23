import { Component, OnInit } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';
import { PageResult } from '@shared/model/page';

import { UAV } from '@site/model/uav';
import { UAVService } from '@site/service/uav.service';
import { UAVComponent } from './uav.component';
import { Router } from '@angular/router';

@Component({
    selector: 'uavs',
    templateUrl: './uavs.component.html',
    styles: ['./uavs.css']
})
export class UAVsComponent implements OnInit {
    res: PageResult<UAV> = {
        resultSet: [],
        count: 0,
        pageNumber: 1,
        pageSize: 10
    };
    bsModalRef: BsModalRef;
    message: string = null;

    constructor(private service: UAVService, private router: Router, private modalService: BsModalService) { }

    ngOnInit(): void {
        this.service.page(1).then(res => {
            this.res = res;
        });
    }

    remove(uav: UAV): void {
        this.service.remove(uav.oid).then(response => {
            this.res.resultSet = this.res.resultSet.filter(h => h.oid !== uav.oid);
        });
    }

    onClickRemove(uav: UAV): void {
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

    view(uav: UAV): void {
        this.router.navigate(['/site/uav', uav.oid]);
    }

    newInstance(): void {
        this.router.navigate(['/site/uav', '__NEW__']);
    }

    showModal(uav: UAV, newInstance: boolean): void {
        this.bsModalRef = this.modalService.show(UAVComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        });
        this.bsModalRef.content.uav = uav;
        this.bsModalRef.content.newInstance = newInstance;

        let that = this;
        this.bsModalRef.content.onUAVChange.subscribe(data => {
            this.onPageChange(this.res.pageNumber);
        });

    }

    onPageChange(pageNumber: number): void {
        this.service.page(pageNumber).then(res => {
            this.res = res;
        });
    }
}
