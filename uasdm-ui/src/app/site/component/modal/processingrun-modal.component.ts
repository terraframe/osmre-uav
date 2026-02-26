///
///
///

import { CommonModule, NgIf } from '@angular/common';  
import { BrowserModule } from '@angular/platform-browser';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

import { ErrorHandler } from '@shared/component';

import { SiteEntity, UploadForm, Task, Selection, CollectionArtifacts, ProcessConfig } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';
import { environment } from 'src/environments/environment';

import {
	fadeInOnEnterAnimation,
	fadeOutOnLeaveAnimation
} from 'angular-animations';
import { HttpErrorResponse } from '@angular/common/http';
import { ODMRun, ProcessingRun } from '@site/model/odmrun';
import { FormsModule } from '@angular/forms';
import { BooleanFieldComponent } from '@shared/component/boolean-field/boolean-field.component';

@Component({
    standalone: true,
    selector: 'processingrun-modal',
    templateUrl: './processingrun-modal.component.html',
    styleUrls: ['./processingrun-modal.component.css'],
    animations: [
        fadeInOnEnterAnimation(),
        fadeOutOnLeaveAnimation()
    ],
    imports: [NgIf, FormsModule, BooleanFieldComponent]
})
export class ProcessingRunModalComponent implements OnInit, OnDestroy {
	message: string = "";
	
	processingRun: ProcessingRun = null;
	
	config: ProcessConfig = null;

	public loading: boolean = true;
	
	constructor(private service: ManagementService, private modalService: BsModalService, public bsModalRef: BsModalRef) {
	}

	ngOnInit(): void {
		
	}

	ngOnDestroy(): void {
	}

	ngAfterViewInit() {

	}

	initOnArtifact(artifact: SiteEntity): void {
		this.loading = true;
		this.service.getProcessingRunByArtifact(artifact.id).then(odmRun => {
			this.processingRun = odmRun;
			this.config = odmRun.config;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		}).finally(() => { this.loading = false; })
	}
	
	initOnWorkflowTask(task: Task): void {
		this.service.getProcessingRunByTask(task.oid).then(odmRun => {
			this.processingRun = odmRun;
			this.config = odmRun.config;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		}).finally(() => { this.loading = false; })
	}
	
	formatDate(date: string): string {
		return new Date(date).toString();
	}
	
	getRuntime(): string {
		var delta = Math.abs(new Date(this.processingRun.runEnd).getTime() - new Date(this.processingRun.runStart).getTime()) / 1000;

		// calculate (and subtract) whole days
		var days = Math.floor(delta / 86400);
		delta -= days * 86400;
		
		// calculate (and subtract) whole hours
		var hours = Math.floor(delta / 3600) % 24;
		delta -= hours * 3600;
		
		// calculate (and subtract) whole minutes
		var minutes = Math.floor(delta / 60) % 60;
		delta -= minutes * 60;
		
		// what's left is seconds
		var seconds = Math.floor(delta) % 60;
		
		if (days > 0) {
			return days + " days " + hours + " hours " + minutes + " minutes " + seconds + " seconds";
		} else if (hours > 0) {
			return hours + " hours " + minutes + " minutes " + seconds + " seconds";
		} else if (minutes > 0) {
			return minutes + " minutes " + seconds + " seconds";
		} else {
			return seconds + " seconds";
		}
	}
	
	downloadReport(): void {
		// window.location.href = environment.apiUrl + '/api/project/download-report?colId=' + this.artifact.id + "&folder=" + this.artifact.folder;
		window.location.href = environment.apiUrl + '/api/project/download?id=' + this.processingRun.report.component + "&key=" + this.processingRun.report.key;
	}

	close(): void {
		this.bsModalRef.hide();
	}

	error(err: any): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}
}
