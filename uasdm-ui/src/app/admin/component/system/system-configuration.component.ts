///
///
///

import { Component, OnInit } from '@angular/core';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { EmailComponent } from '@admin/component/email/email.component';

import { AuthService } from '@shared/service/auth.service';
import { Organization } from '@shared/model/organization';
import { OrganizationService } from '@shared/service/organization.service';
import { OrganizationHierarchyModalComponent } from '../../../shared/component/organization-field/organization-hierarchy-modal.component';
import { ConfigurationService } from '@core/service/configuration.service';
import { PageResult } from '@shared/model/page';
import { HttpErrorResponse } from '@angular/common/http';
import { ErrorHandler } from '@shared/component';

@Component({
    standalone: false,
  selector: 'system-configuration',
    templateUrl: './system-configuration.component.html',
    styleUrls: []
})
export class SystemConfigurationComponent implements OnInit {

    userName: string = "";
    admin: boolean = false;

    private bsModalRef: BsModalRef;
    organizations: PageResult<Organization> = {
        resultSet: [],
        count: 0,
        pageNumber: 1,
        pageSize: 10
    };

    requireKeycloakLogin: boolean;

    constructor(
        private configuration: ConfigurationService,
        private modalService: BsModalService,
        private orgService: OrganizationService,
        private authService: AuthService) {
        this.requireKeycloakLogin = configuration.isRequireKeycloakLogin();
    }


    ngOnInit(): void {
        this.userName = this.authService.getUserName();
        this.admin = this.authService.isAdmin();

        this.onOrgPageChange(1);
    }

    onOrgPageChange(pageNumber: number): void {
        this.orgService.page(pageNumber, this.organizations.pageSize).then(oPage => {
            this.organizations = oPage;
        });
    }


    open(): void {
        this.bsModalRef = this.modalService.show(EmailComponent, {
            animated: true,
            backdrop: true, class: 'modal-xl',
            ignoreBackdropClick: true,
        });

        (<EmailComponent>this.bsModalRef.content).onSuccess.subscribe(data => {
            this.bsModalRef.hide();
        });
    }

    onManageHierarchy(): void {
        this.bsModalRef = this.modalService.show(OrganizationHierarchyModalComponent, {
            animated: true,
            backdrop: true, class: 'modal-xl',
            ignoreBackdropClick: true,
        });
        this.bsModalRef.content.init(true, null, null);
    }

}
