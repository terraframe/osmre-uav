///
///
///

import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { BasicConfirmModalComponent, ErrorHandler, NotificationModalComponent } from '@shared/component';

import { AuthService } from '@shared/service/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { LPGSyncService } from '@shared/service/lpg-sync.service';
import { LabeledPropertyGraphType, LabeledPropertyGraphTypeEntry, LabeledPropertyGraphTypeVersion, LPGSync } from '@shared/model/lpg';
import { BsModalService } from 'ngx-bootstrap/modal';
import { WebSocketSubject, webSocket } from 'rxjs/webSocket';
import { WebSockets } from '@core/utility/web-sockets';
import { EventService } from '@shared/service/event.service';
import { ModalTypes } from '@shared/model/modal';

@Component({
    selector: 'labeled-property-graph-sync',
    templateUrl: './labeled-property-graph-sync.component.html',
    styleUrls: []
})
export class LPGSyncComponent implements OnInit, OnDestroy {

    isAdmin: boolean = false;

    original: LPGSync;
    sync: LPGSync;
    newInstance: boolean = false;

    message: string = null;

    mode: string = 'READ';

    types: any[] = null;
    entries: LabeledPropertyGraphTypeEntry[] = null;
    versions: LabeledPropertyGraphTypeVersion[] = null;

    notifier: WebSocketSubject<any>;

    constructor(
        private service: LPGSyncService,
        private eventService: EventService,
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

                this.notifier = webSocket(WebSockets.buildBaseUrl() + "/websocket-notifier/notify");
                this.notifier.subscribe(message => {
                    if (message.type === "SYNCHRONIZATION_JOB_CHANGE") {
                        this.handleJobUpdate(message.content);
                    }
                });

                // Check if there is a job already in progress
                this.handleJobUpdate({ oid: sync.oid });
            });


        }
    }

    ngOnDestroy(): void {
        if (this.notifier != null) {
            this.notifier.complete();
        }
    }

    handleJobUpdate(content: { oid: string }): void {
        if (content.oid === this.sync.oid) {
            this.service.getStatus(this.sync.oid).then(response => {

                if (response.status === 'NEW' || response.status === 'QUEUED' || response.status === 'RUNNING') {
                    // Start the spinner
                    this.eventService.start();
                }
                else {
                    this.eventService.complete();

                    if (response.error != null) {
                        this.message = ErrorHandler.getMessageFromError(response.error);
                    }
                }
            })
        }
    }

    handleURLChange(): void {
        if (this.sync.url != null && this.sync.url.length > 0) {

            const urlPattern = /(?:https?):\/\/(\w+:?\w*)?(\S+)(:\d+)?(\/|\/([\w#!:.?+=&%!\-\/]))?/;

            const valid = !!urlPattern.test(this.sync.url);

            if (valid) {
                this.service.getTypes(this.sync.url).then((types: any[]) => {
                    this.types = types;
                }).catch(e => {
                    ErrorHandler.showErrorAsDialog({ message: 'Unable to communicate with the remote server. Please check that the URL corresponds to a valid GPR server. The server responsed with the error code [' + e.status + ']' }, this.modalService);
                })
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
            }).catch(e => {
                ErrorHandler.showErrorAsDialog({ message: 'Unable to communicate with the remote server. Please check that the URL corresponds to a valid GPR server. The server responsed with the error code [' + e.status + ']' }, this.modalService);
            })
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
            }).catch(e => {
                ErrorHandler.showErrorAsDialog({ message: 'Unable to communicate with the remote server. Please check that the URL corresponds to a valid GPR server. The server responsed with the error code [' + e.status + ']' }, this.modalService);
            })
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
                this.router.navigate(['/admin/lpg-sync', data.oid]);
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
            this.handleJobUpdate({ oid: this.sync.oid });
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleCreateTiles(): void {
        this.message = null;

        if (this.sync.version != null) {
            this.service.createTiles(this.sync.version).then(data => {
                this.handleJobUpdate({ oid: this.sync.oid });
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        }
    }


    handleOnCheckVersion(): void {
        this.service.getVersions(this.sync.url, this.sync.remoteEntry).then((v: LabeledPropertyGraphTypeVersion[]) => {
            const versions = v.filter((version) => version.versionNumber > this.sync.versionNumber);

            if (versions.length > 0) {
                const version = versions[versions.length - 1];

                const bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
                    animated: false,
                    backdrop: true, class: 'modal-xl',
                    ignoreBackdropClick: true,
                });
                bsModalRef.content.message = "A new version of the labeled property graph has been released.  Do you want to upgrade to version [" + version.versionNumber + "]? This action cannot be undone.";
                bsModalRef.content.type = ModalTypes.danger;
                bsModalRef.content.submitText = "Upgrade";

                bsModalRef.content.onConfirm.subscribe(data => {
                    this.service.updateRemoteVersion(this.sync.oid, version.oid, version.versionNumber).then(sync => {
                        this.sync = sync;

                        this.handleOnSynchronize();
                    })
                });
            }
            else {
                const bsModalRef = this.modalService.show(NotificationModalComponent, {
                    animated: false,
                    backdrop: true, class: 'modal-xl',
                    ignoreBackdropClick: true,
                });
                bsModalRef.content.messageTitle = "";
                bsModalRef.content.message = "No newer versions were found."
                bsModalRef.content.submitText = "OK";
            }
        }).catch((err: HttpErrorResponse) => {
            ErrorHandler.showErrorAsDialog({ message: 'Unable to communicate with the remote server. Please check that the URL corresponds to a valid GPR server. The server responsed with the error code [' + err.status + ']' }, this.modalService);
        });
    }



    isValid(): boolean {
        if (this.sync.url == null || this.sync.url.length == 0) {
            return false;
        }

        if (this.sync.remoteType == null || this.sync.remoteType.length == 0) {
            return false;
        }

        if (this.sync.remoteEntry == null || this.sync.remoteEntry.length == 0) {
            return false;
        }

        if (this.sync.remoteVersion == null || this.sync.remoteVersion.length == 0) {
            return false;
        }

        return true;
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
