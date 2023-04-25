import { Component, OnInit, AfterViewInit, Inject, ViewChild, ElementRef, KeyValueDiffers, DoCheck, HostListener } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { interval } from 'rxjs';
import { switchMap, map } from 'rxjs/operators';

//use Fine Uploader UI for traditional endpoints
import { FineUploader, UIOptions } from 'fine-uploader';

import { ErrorModalComponent } from '@shared/component/modal/error-modal.component';
import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';
import { SiteEntity, UploadForm, Task } from '../model/management';
import { ManagementService } from '../service/management.service';
import EnvironmentUtil from '@core/utility/environment-util';

@Component({
    selector: 'upload',
    templateUrl: './upload.component.html',
    styleUrls: []
})
export class UploadComponent implements OnInit {

    /*
     * Reference to the modal current showing
     */
    private bsModalRef: BsModalRef;

    /* 
     * List of sites
     */
    sites = [] as SiteEntity[];

    /* 
     * List of projects
     */
    projects = [] as SiteEntity[];

    /* 
     * List of missions
     */
    missions = [] as SiteEntity[];

    /* 
     * List of collections
     */
    collections = [] as SiteEntity[];

    /* 
     * Form values
     */
    values: UploadForm = {
        create: false,
        includeGeoLocationFile: false,
        geoLocationFormat:'RX1R2'
    };

    /*
     * FineUploader for uploading large files
     */
    uploader = null as FineUploader;

    disabled: boolean = false;
    taskStatusMessages: string[] = [];
    currentTask: Task = null;
    existingTask: boolean = false;
    taskPolling: any;
    pollingIsSet: boolean = false;
    uploadVisible: boolean = true;
    selectedContinue: boolean = false;
    uploadCounter: string = "00:00:00";
    uplodeCounterInterfal: any;
    differ: any;
    showFileSelectPanel: boolean = false;
    taskFinishedNotifications: any[] = [];

    constructor(private service: ManagementService, private modalService: BsModalService, differs: KeyValueDiffers) {
        this.differ = differs.find([]).create();
    }

    ngDoCheck() {

        if (this.uploader) {
            const change = this.differ.diff(this.uploader);
            if (change) {
                this.setExistingTask();
            }
        }
    }

    @ViewChild('uploader') set content(elem: ElementRef) {

        const that = this;

        if (elem != null && this.uploader == null) {

            let uiOptions: UIOptions = {
                debug: false,
                autoUpload: false,
                multiple: false,
                element: elem.nativeElement,
                template: 'qq-template',
                request: {
                    endpoint: EnvironmentUtil.getApiUrl() + "/file/upload",
                    forceMultipart: true
                },
                resume: {
                    enabled: true,
                    recordsExpireIn: 1
                },
                chunking: {
                    enabled: true
                },
                retry: {
                    enableAuto: true
                },
                text: {
                    defaultResponseError: "Upload failed"
                },
                failedUploadTextDisplay: {
                    mode: 'none'
                    //responseProperty: 'error'
                },
                validation: {
                    allowedExtensions: ['zip', 'tar.gz']
                },
                showMessage: function (message: string) {
                    // 
                },
                callbacks: {
                    onUpload: function (id: any, name: any): void {
                        that.disabled = true;

                        // document.getElementById("select-file-button").className = document.getElementById("select-file-button").className + " hidden";

                        that.countUpload(that);
                    },
                    onProgress: function (id: any, name: any, uploadedBytes: any, totalBytes: any): void {
                    },
                    onUploadChunk: function (id: any, name: any, chunkData: any): void {
                    },
                    onUploadChunkSuccess: function (id: any, chunkData: any, responseJSON: any, xhr: any): void {

                        if (responseJSON.message && responseJSON.message.currentTask && !that.currentTask) {
                            that.currentTask = responseJSON.message.currentTask;
                        }

                        if (that.currentTask && !that.pollingIsSet) {
                            that.pollingIsSet = true;

                            that.taskPolling = interval(2000)
                                .pipe(switchMap(() => {
                                    if (that.currentTask) {
                                        return that.service.task(that.currentTask.oid);
                                    }
                                }))
                                .pipe(map((data) => data))
                                .subscribe((data) => {
                                    that.currentTask = data.task
                                });
                        }
                    },
                    onComplete: function (id: any, name: any, responseJSON: any, xhrOrXdr: any): void {
                        that.disabled = false;
                        that.currentTask = null;
                        that.existingTask = false;

                        if (that.taskPolling) {
                            that.taskPolling.unsubscribe();
                            that.pollingIsSet = false;
                        }

                        this.clearStoredFiles();

                        // document.getElementById("select-file-button").classList.remove("hidden");

                        clearInterval(that.uplodeCounterInterfal);

                        that.taskFinishedNotifications.push(
                            {
                                'id': id
                            }
                        )
                    },
                    onCancel: function (id: number, name: string) {
                        //that.currentTask = null;

                        if (that.currentTask && that.currentTask.uploadId) {
                            that.service.removeTask(that.currentTask.uploadId)
                                .then(() => {
                                    this.clearStoredFiles();
                                })
                                .catch((err: HttpErrorResponse) => {
                                    this.error(err);
                                });
                        }

                        that.disabled = false;
                        that.currentTask = null;
                        that.existingTask = false;

                        if (that.taskPolling) {
                            that.taskPolling.unsubscribe();
                            that.pollingIsSet = false;
                        }

                        // document.getElementById("select-file-button").classList.remove("hidden");

                        clearInterval(that.uplodeCounterInterfal);
                    },
                    onError: function (id: number, errorReason: string, xhrOrXdr: string) {
                        that.error(xhrOrXdr);
                    }
                }
            };

            this.uploader = new FineUploader(uiOptions);

        }
    }

    ngAfterViewInit() {

    }

    ngOnInit(): void {
        this.service.roots(null, null).then(sites => {
            this.sites = sites;
        });
    }

    closeTaskFinishedNotification(id: string): void {
        // iterate in reverse to allow splice while avoiding the reindex
        // from affecting any of the next items in the array.
        let i = this.taskFinishedNotifications.length;
        while (i--) {
            let note = this.taskFinishedNotifications[i];
            if (id === note.id) {
                this.taskFinishedNotifications.splice(i, 1);
            }
        }
    }


    setExistingTask(): void {
        let resumable = this.uploader.getResumableFilesData() as any[];
        if (resumable.length > 0) {
            this.existingTask = true;

            if (!this.selectedContinue) {
                this.hideUploadPanel();
            }
        }
    }

    onSiteSelect(target: EventTarget): void {
        const siteId = (<HTMLTextAreaElement>target).value;

        this.values.site = siteId;

        if (siteId != null && siteId.length > 0) {

            // Reset select options
            this.projects = [] as SiteEntity[];
            this.missions = [] as SiteEntity[];
            this.collections = [] as SiteEntity[];

            // Reset form values
            this.values.project = null;
            this.values.mission = null;
            this.values.collection = null;

            this.service.getChildren(this.values.site).then(projects => {
                this.projects = projects;
            });
        }

        //console.log( this.values );
    }

    onProjectSelect(target: EventTarget): void {
        const projectId = (<HTMLTextAreaElement>target).value;

        this.values.project = projectId;

        // Reset select options
        this.missions = [] as SiteEntity[];
        this.collections = [] as SiteEntity[];

        // Reset form values
        this.values.mission = null;
        this.values.collection = null;

        if (projectId != null && projectId.length > 0) {
            this.service.getChildren(this.values.project).then(missions => {
                this.missions = missions.filter(mission => {
                    return mission.type === 'Mission';
                });
            });
        }
    }

    onMissionSelect(target: EventTarget): void {
        const missionId = (<HTMLTextAreaElement>target).value;

        this.values.mission = missionId;

        // Reset select options
        this.collections = [] as SiteEntity[];

        // Reset form values
        this.values.collection = null;
        this.values.name = null;

        if (missionId != null && missionId.length > 0 && !this.values.create) {

            this.service.getChildren(this.values.mission).then(collections => {
                this.collections = collections;
            });
        }
    }

    handleChange(): void {

        // Reset select options
        this.collections = [] as SiteEntity[];

        // Reset form values
        this.values.collection = null;
        this.values.name = null;

        if (this.values.mission != null && this.values.mission.length > 0 && !this.values.create) {

            this.service.getChildren(this.values.mission).then(collections => {
                this.collections = collections;
            });
        }
    }

    onCollectionSelect(target: EventTarget): void {
        const collectionId = (<HTMLTextAreaElement>target).value;
        this.values.collection = collectionId;
        this.values.uasComponentOid = collectionId;

        if (collectionId && collectionId.trim().length > 0) {
            this.showFileSelectPanel = true;
        }
        else {
            this.showFileSelectPanel = false
        }
    }

    handleUpload(): void {

        /*
         * Validate form values before uploading
         */
        if (!this.values.create && this.values.collection == null && !this.existingTask) {
            this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
            this.bsModalRef.content.message = "A collection must first be selected before the file can be uploaded";
        }
        else if (this.values.create && (this.values.mission == null || this.values.name == null || this.values.name.length == 0) && !this.existingTask) {
            this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
            this.bsModalRef.content.message = "Name is required";
        }
        else {
            this.uploader.setParams(this.values);
            this.uploader.uploadStoredFiles();
        }

    }

    removeUpload(event: any): void {
        let that = this;

        this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        });
        this.bsModalRef.content.message = 'Are you sure you want to cancel the upload of [' + this.uploader.getResumableFilesData()[0].name + ']';
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = 'Cancel Upload';

        (<BasicConfirmModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
            this.service.removeTask(this.uploader.getResumableFilesData()[0].uuid)
                .then(() => {
                    //that.uploader.clearStoredFiles();
                    //that.uploader.cancelAll()

                    // The above clearStoredFiles() and cancelAll() methods don't appear to work so 
                    // we are clearing localStorage manually.
                    localStorage.clear();
                    that.existingTask = false;
                    that.showUploadPanel();

                });
        });
    }

    hideUploadPanel(): void {
        this.uploadVisible = false;
    }

    showUploadPanel(): void {
        this.uploadVisible = true;
        this.selectedContinue = true;
    }

    countUpload(thisRef: any): void {
        let ct = 0;


        function incrementSeconds() {
            ct += 1;

            let hours = Math.floor(ct / 3600)
            let minutes = Math.floor((ct % 3600) / 60);
            let seconds = Math.floor(ct % 60);

            let hoursStr = minutes < 10 ? "0" + hours : hours;
            let minutesStr = minutes < 10 ? "0" + minutes : minutes;
            let secondsStr = seconds < 10 ? "0" + seconds : seconds;

            thisRef.uploadCounter = hoursStr + ":" + minutesStr + ":" + secondsStr;
        }

        thisRef.uplodeCounterInterfal = setInterval(incrementSeconds, 1000);
    }

    public canDeactivate(): boolean {
        return this.disabled;
    }

    @HostListener('window:beforeunload', ['$event'])
    unloadNotification($event: any) {
        if (this.disabled) {
            $event.returnValue = 'An upload is currently in progress. Are you sure you want to leave?';
        }
    }

    error(message: string): void {
        if (message !== null) {
            this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
            this.bsModalRef.content.message = message;
        }
    }
}
