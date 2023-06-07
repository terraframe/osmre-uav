///
///
///

import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { ErrorHandler } from '@shared/component';

import { AuthService } from '@shared/service/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { LPGSyncService } from '@site/service/lpg-sync.service';
import { LabeledPropertyGraphType, LabeledPropertyGraphTypeEntry, LabeledPropertyGraphTypeVersion, LPGSync } from '@site/model/lpg-sync';
import { BsModalService } from 'ngx-bootstrap/modal';

@Component({
    selector: 'labeled-property-graph-sync',
    templateUrl: './labeled-property-graph-sync.component.html',
    styleUrls: []
})
export class LPGSyncComponent implements OnInit {

    isAdmin: boolean = false;

    original: LPGSync;
    sync: LPGSync;
    newInstance: boolean = false;

    message: string = null;

    mode: string = 'READ';

    types: any[] = null;
    entries: LabeledPropertyGraphTypeEntry[] = null;
    versions: LabeledPropertyGraphTypeVersion[] = null;

    constructor(
        private service: LPGSyncService,
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
            this.service.newInstance().then((sync: LPGSync) => {
                this.sync = sync;
                this.newInstance = true;
                this.mode = 'WRITE';
            });
        }
        else {
            this.service.get(oid).then((sync: LPGSync) => {
                this.sync = sync;
                this.original = JSON.parse(JSON.stringify(this.sync));
            });
        }
    }

    handleURLChange(): void {
        if (this.sync.url != null && this.sync.url.length > 0) {

            const urlPattern = /(?:https?):\/\/(\w+:?\w*)?(\S+)(:\d+)?(\/|\/([\w#!:.?+=&%!\-\/]))?/;

            const valid = !!urlPattern.test(this.sync.url);

            if (valid) {
                this.service.getTypes(this.sync.url).then((types: any[]) => {
                    this.types = types;
                });
            }
            else {
                ErrorHandler.showErrorAsDialog({ message: 'Invalid URL format' }, this.modalService);
            }
        }
    }

    handleTypeChange(): void {
        if (this.sync.remoteType != null && this.sync.remoteType.length > 0) {

            this.service.getEntries(this.sync.url, this.sync.remoteType).then((type: LabeledPropertyGraphType) => {
                this.sync.displayLabel = type.displayLabel;
                this.entries = type.entries;
            });
        }
    }

    handleEntryChange(): void {
        if (this.sync.remoteEntry != null && this.sync.remoteEntry.length > 0) {
            const entry = this.entries.find(x => x.oid === this.sync.remoteEntry);

            if (entry != null) {
                this.sync.forDate = entry.forDate;
            }

            this.service.getVersions(this.sync.url, this.sync.remoteEntry).then((versions: LabeledPropertyGraphTypeVersion[]) => {
                this.versions = versions;
            });
        }
    }

    handleVersionChange(): void {
        if (this.sync.remoteVersion != null && this.sync.remoteVersion.length > 0) {
            const version = this.versions.find(x => x.oid === this.sync.remoteVersion);

            if (version != null) {
                this.sync.versionNumber = version.versionNumber;
            }
        }
    }


    handleOnSubmit(): void {
        this.message = null;

        this.service.apply(this.sync).then(data => {
            this.sync = data;
            this.mode = 'READ';

            if (this.newInstance) {
                this.router.navigate(['/site/lpg-syncs', data.oid]);
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
            alert("Finished synchronizing")
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }


    isValid(): boolean {
        if (this.sync.url == null && this.sync.url.length == 0) {
            return false;
        }

        if (this.sync.remoteType == null && this.sync.remoteType.length == 0) {
            return false;
        }

        if (this.sync.remoteEntry == null && this.sync.remoteEntry.length == 0) {
            return false;
        }

        if (this.sync.remoteVersion == null && this.sync.remoteVersion.length == 0) {
            return false;
        }

        return true;
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
