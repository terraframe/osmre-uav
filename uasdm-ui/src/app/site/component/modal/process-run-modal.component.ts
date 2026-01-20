///
///
///

import { CommonModule, NgIf } from '@angular/common';
import { BrowserModule } from '@angular/platform-browser';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

import { ErrorHandler } from '@shared/component';

import { SiteEntity, UploadForm, Task, Selection, CollectionArtifacts, ProcessConfig, ProcessConfigType } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';
import { environment } from 'src/environments/environment';

import {
	fadeInOnEnterAnimation,
	fadeOutOnLeaveAnimation
} from 'angular-animations';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { BooleanFieldComponent } from '../../../shared/component/boolean-field/boolean-field.component';

@Component({
    standalone: true,
    selector: 'process-run-modal',
    templateUrl: './process-run-modal.component.html',
    styleUrls: [],
    animations: [
        fadeInOnEnterAnimation(),
        fadeOutOnLeaveAnimation()
    ],
    imports: [NgIf, FormsModule, BooleanFieldComponent]
})
export class ProcessRunModalComponent implements OnInit, OnDestroy {
	message: string = "";

	configuration: ProcessConfig = null;

	// Make the process config type usable in the HTML template
	readonly ProcessConfigType = ProcessConfigType;


	constructor(private service: ManagementService, public bsModalRef: BsModalRef) {
	}

	ngOnInit(): void {

	}

	ngOnDestroy(): void {
	}

	ngAfterViewInit() {

	}

	init(task: Task): void {
		this.service.getConfigurationByTask(task.oid).then(configuration => {
			this.configuration = configuration;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	close(): void {
		this.bsModalRef.hide();
	}

	error(err: any): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}
}
