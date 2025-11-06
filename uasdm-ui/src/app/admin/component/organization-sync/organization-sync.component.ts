///
///
///

import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { BasicConfirmModalComponent, ErrorHandler, NotificationModalComponent } from '@shared/component';

import { AuthService } from '@shared/service/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { OrganizationSync } from '@shared/model/organization';
import { OrganizationSyncService } from '@shared/service/organization-sync.service';

@Component({
    standalone: false,
  selector: 'organization-sync',
    templateUrl: './organization-sync.component.html',
    styleUrls: []
})
export class OrganizationSyncComponent implements OnInit {

    isAdmin: boolean = false;

    original: OrganizationSync;
    sync: OrganizationSync;
    newInstance: boolean = false;

    message: string = null;

    mode: string = 'READ';

    constructor(
        private service: OrganizationSyncService,
        private authService: AuthService,
        private modalService: BsModalService,
        private route: ActivatedRoute,
        private router: Router
    ) {
        this.isAdmin = this.authService.isAdmin();
    }

    ngOnInit(): void {
        const oid = this.route.snapshot.params['oid'];

        if (oid === '__NEW__') {
            this.service.newInstance().then((sync: OrganizationSync) => {
                this.sync = sync;
                this.newInstance = true;
                this.mode = 'WRITE';
            });
        }
        else {
            this.service.get(oid).then((sync: OrganizationSync) => {
                this.sync = sync;
                this.original = JSON.parse(JSON.stringify(this.sync));
            });
        }
    }



    handleOnSubmit(): void {
        this.message = null;

        this.service.apply(this.sync).then(data => {
            this.sync = data;
            this.mode = 'READ';

            if (this.newInstance) {
                this.router.navigate(['/admin/organization-sync', data.oid]);
                this.newInstance = false;
                this.original = data;
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleOnCancel(): void {
        this.message = null;

        this.sync = JSON.parse(JSON.stringify(this.original));
        this.mode = 'READ';
    }

    handleOnEdit(): void {
        this.mode = 'WRITE';
    }

    handleOnSynchronize(): void {
        this.message = null;

        this.service.execute(this.sync.oid).then(data => {
            const bsModalRef = this.modalService.show(NotificationModalComponent, {
                animated: true,
                backdrop: true, class: 'modal-xl',
                ignoreBackdropClick: true,
            });
            bsModalRef.content.messageTitle = "";
            bsModalRef.content.message = "Finished synchronizing with the remote server."
            bsModalRef.content.submitText = "OK";
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    isValid(): boolean {
        if (this.sync.url == null || this.sync.url.length == 0) {
            return false;
        }

        return true;
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
