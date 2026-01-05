///
///
///

import { CommonModule } from '@angular/common';  
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
import { ODMRun } from '@site/model/odmrun';

@Component({
	standalone: false,
  selector: 'odmrun-modal',
	templateUrl: './odmrun-modal.component.html',
	styleUrls: ['./odmrun-modal.component.css'],
	animations: [
		fadeInOnEnterAnimation(),
		fadeOutOnLeaveAnimation()
	]
})
export class ODMRunModalComponent implements OnInit, OnDestroy {
	message: string = "";
	
	odmRun: ODMRun = null;
	
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
		this.service.getODMRunByArtifact(artifact.id).then(odmRun => {
			this.odmRun = odmRun;
			this.config = odmRun.config;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		}).finally(() => { this.loading = false; })
	}
	
	initOnWorkflowTask(task: Task): void {
		this.service.getODMRunByTask(task.oid).then(odmRun => {
			this.odmRun = odmRun;
			this.config = odmRun.config;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		}).finally(() => { this.loading = false; })
	}
	
	formatDate(date: string): string {
		return new Date(date).toString();
	}
	
	getRuntime(): string {
		var delta = Math.abs(new Date(this.odmRun.runEnd).getTime() - new Date(this.odmRun.runStart).getTime()) / 1000;

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
		window.location.href = environment.apiUrl + '/api/project/download?id=' + this.odmRun.report.component + "&key=" + this.odmRun.report.key;
	}

	close(): void {
		this.bsModalRef.hide();
	}

	error(err: any): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}
}
