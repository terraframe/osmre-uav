import { Component, OnInit } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';
import { PageResult } from '@shared/model/page';

import { Platform } from '@site/model/platform';
import { PlatformService } from '@site/service/platform.service';
import { PlatformComponent } from './platform.component';
import { Router } from '@angular/router';

@Component({
    selector: 'platforms',
    templateUrl: './platforms.component.html',
    styles: ['./platforms.css']
})
export class PlatformsComponent implements OnInit {
    res: PageResult<Platform> = {
        resultSet: [],
        count: 0,
        pageNumber: 1,
        pageSize: 10
    };
    bsModalRef: BsModalRef;
    message: string = null;

    constructor(private service: PlatformService, private router: Router, private modalService: BsModalService) { }

    ngOnInit(): void {
        this.service.page(1).then(res => {
            this.res = res;
        });
    }

    remove(platform: Platform): void {
        this.service.remove(platform.oid).then(response => {
            this.res.resultSet = this.res.resultSet.filter(h => h.oid !== platform.oid);
        });
    }

    onClickRemove(platform: Platform): void {
        this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        });
        this.bsModalRef.content.message = "Are you sure you want to remove the platform [" + platform.name + "]";
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = "Delete";

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.remove(platform);
        });
    }

    view(platform: Platform): void {
        this.router.navigate(['/site/platform', platform.oid]);
    }

    newInstance(): void {
        this.router.navigate(['/site/platform', '__NEW__']);
    }

    showModal(platform: Platform, newInstance: boolean): void {
        this.bsModalRef = this.modalService.show(PlatformComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        });
        this.bsModalRef.content.platform = platform;
        this.bsModalRef.content.newInstance = newInstance;

        let that = this;
        this.bsModalRef.content.onPlatformChange.subscribe(data => {
            this.onPageChange(this.res.pageNumber);
        });

    }

    onPageChange(pageNumber: number): void {
        this.service.page(pageNumber).then(res => {
            this.res = res;
        });
    }
}
