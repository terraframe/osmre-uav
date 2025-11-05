///
///
///

import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';
import { LocalizationService } from '@shared/service/localization.service';

import { SystemLogo } from '../../model/system-logo';
import { SystemLogoService } from '../../service/system-logo.service';
import { SystemLogoComponent } from './system-logo.component';
import EnvironmentUtil from '@core/utility/environment-util';

@Component({

    standalone: false,
  selector: 'system-logos',
    templateUrl: './system-logos.component.html',
    styleUrls: []
})
export class SystemLogosComponent implements OnInit {
    public icons: SystemLogo[];
    context: string;
    bsModalRef: BsModalRef;
    message: string = null;

    constructor(
        private router: Router,
        private service: SystemLogoService,
        private modalService: BsModalService,
        private localizeService: LocalizationService) {

        this.context = EnvironmentUtil.getApiUrl();
    }

    ngOnInit(): void {
        this.getIcons();
    }

    onClickRemove(icon): void {
        this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
            animated: true,
            backdrop: true, class: 'modal-xl',
            ignoreBackdropClick: true,
        });
        this.bsModalRef.content.message = this.localizeService.decode("system.image.removeContent");
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = 'Delete';

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.remove(icon);
        });
    }

    getIcons(): void {
        this.service.getIcons().then(icons => {

            var filtered = icons.filter(function (el) { return el.oid != "banner"; });
            this.icons = filtered;
        });
    }

    edit(icon: SystemLogo): void {
        // this.router.navigate( ['/admin/logo', icon.oid] );

        let bsModalRef = this.modalService.show(SystemLogoComponent, {
            animated: true,
            backdrop: true, class: 'modal-xl',
            ignoreBackdropClick: true,
        });

        bsModalRef.content.onSuccess.subscribe(data => {

            /*
            this.icons.forEach(ico => {
      
              // Setting a random number at the end of the url is a hack to change 
              // the image url to force Angular to rerender the image.
              this.random = Math.random();
      
              ico.oid = ico.oid
            })
      
            this.changeDetectorRef.detectChanges();
            */
            window.location.reload();
        });
    }

    remove(icon: SystemLogo): void {
        this.service.remove(icon.oid).then(response => {
            icon.custom = false;
            window.location.reload();
        });
    }
}
