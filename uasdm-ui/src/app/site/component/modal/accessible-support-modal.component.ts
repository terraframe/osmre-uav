///
///
///

import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { BsModalService } from 'ngx-bootstrap/modal';
import { NgxFileDropEntry, FileSystemFileEntry, FileSystemDirectoryEntry } from 'ngx-file-drop';

import { ErrorHandler, BasicConfirmModalComponent } from '@shared/component';

import { SiteEntity, SiteObjectsResultSet } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';
import { environment } from 'src/environments/environment';

@Component({
    selector: 'accessible-support-modal',
    templateUrl: './accessible-support-modal.component.html',
    styleUrls: [],
})
export class AccessibleSupportModalComponent implements OnInit {

    /* 
     * Breadcrumb of previous sites clicked on
     */
    previous = [] as SiteEntity[];
    folders: SiteEntity[] = [];
    message: string;

    entity: SiteEntity;
    folder: SiteEntity;

    page: SiteObjectsResultSet = new SiteObjectsResultSet();

    constructor(private service: ManagementService, private modalService: BsModalService, public bsModalRef: BsModalRef) {
    }

    ngOnInit(): void {
        this.page.count = 0;
        this.page.pageNumber = 1;
        this.page.pageSize = 10;
        this.page.results = [];
    }

    init(entity: SiteEntity, folders: SiteEntity[], previous: SiteEntity[]): void {

        this.entity = entity;
        this.folders = folders;
        this.previous = [...previous];

        if (this.previous.length > 0 && this.previous[this.previous.length - 1].id !== this.entity.id) {
            this.previous.push(this.entity);
        }

        if (this.folders.length > 0) {
            this.onSelect(this.folders[0]);
        }
    }

    onPageChange(pageNumber: number): void {
        this.getData(this.folder.component, this.folder.name, pageNumber, this.page.pageSize);
    }

    onSelect(folder: SiteEntity): void {

        this.page.results = [];

        this.folder = folder;

        this.getData(folder.component, folder.name, 1, this.page.pageSize);
    }

    refresh(): void {

        this.page.results = [];

        this.getData(this.folder.component, this.folder.name, this.page.pageNumber, this.page.pageSize);
    }

    getData(component: string, folder: string, pageNumber: number, pageSize: number) {
        this.service.getObjects(component, folder, pageNumber, pageSize).then(page => {
            this.page = page;
        });
    }

    handleDownload(): void {
        window.location.href = environment.apiUrl + '/project/download-all?id=' + this.folder.component + "&key=" + this.folder.name;
    }

    handleDownloadFile(item: SiteEntity): void {
        window.location.href = environment.apiUrl + '/project/download?id=' + this.folder.component + "&key=" + item.key;
    }

    dropped(files: NgxFileDropEntry[]): void {

        for (const droppedFile of files) {

            // Is it a file?
            if (droppedFile.fileEntry.isFile) {
                const fileEntry: FileSystemFileEntry = droppedFile.fileEntry as FileSystemFileEntry;

                fileEntry.file((file: File) => {

                    this.service.upload(this.folder.component, null, this.folder.name, file).then(() => {
                        // Refresh the table
                        this.refresh();
                    });

                });
            } else {
                // It was a directory (empty directories are added, otherwise only files)
                const fileEntry: FileSystemDirectoryEntry = droppedFile.fileEntry as FileSystemDirectoryEntry;
                console.log(droppedFile.relativePath, fileEntry);
            }
        }
    }

    handleDelete(item: SiteEntity): void {
        let modalRef: BsModalRef = this.modalService.show(BasicConfirmModalComponent, {
            animated: false,
            backdrop: true, class: 'modal-xl',
            ignoreBackdropClick: true,
        });
        modalRef.content.message = 'Are you sure you want to delete the file [' + item.name + ']?';
        modalRef.content.type = 'DANGER';
        modalRef.content.submitText = 'Delete';

        modalRef.content.onConfirm.subscribe(() => {
            this.remove(item);
        });
    }

    remove(item: SiteEntity): void {
        this.service.removeObject(item.component, item.key).then(() => {
            // Refresh the table
            this.refresh();
        });
    }

    error(err: HttpErrorResponse): void {
      this.message = ErrorHandler.getMessageFromError(err);
    }
}
