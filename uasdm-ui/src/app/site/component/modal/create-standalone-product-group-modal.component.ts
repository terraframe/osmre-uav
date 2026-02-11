///
///
///

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

import { Subject } from 'rxjs';

import { ErrorHandler } from '@shared/component';

import { Sensor } from '@site/model/sensor';
import { Platform } from '@site/model/platform';
import { SiteEntity, Selection, projectTypes } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';
import { MetadataService } from '@site/service/metadata.service';
import { EventService } from '@shared/service/event.service';

import { StepConfig } from '@shared/modal/step-indicator/modal-step-indicator'

import {
	fadeInOnEnterAnimation,
	fadeOutOnLeaveAnimation
} from 'angular-animations';
import { NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MetadataPageComponent } from '../metadata-page/metadata-page.component';

export class Page {
	index?: number;
	selection?: Selection;
	options?: SiteEntity[];
	type?: string
};

@Component({
    standalone: true,
    selector: 'create-standalone-pg-modal',
    templateUrl: './create-standalone-product-group-modal.component.html',
    styleUrls: ['./upload-modal.component.css'],
    animations: [
        fadeInOnEnterAnimation(),
        fadeOutOnLeaveAnimation()
    ],
    imports: [NgIf, FormsModule, NgFor, MetadataPageComponent]
})
export class CreateStandaloneProductModalComponent implements OnInit, OnDestroy {
	message: string = "";

	disabled: boolean = false;
	
	public component;
	
	public projectTypes: string[] = projectTypes;

	/*
	 * Current page  
	 */
	hierarchyChange: boolean = false;

	sensors: Sensor[] = [];
	platforms: Platform[] = [];

	public onCreateComplete: Subject<any>;

	public productGroupName: string;

	public metadata: any = {};

	parents: SiteEntity[];

	modalStepConfig: StepConfig = {
		"steps": [
			{ "label": "Category", "active": true, "enabled": true },
			{ "label": "Final", "active": true, "enabled": false }
		]
	};

	loading: boolean = false;

	constructor(private event: EventService, private service: ManagementService, private metadataService: MetadataService, public bsModalRef: BsModalRef) {
	}


	ngAfterViewInit() {

	}

	ngOnInit(): void {
		this.onCreateComplete = new Subject();
	}

	ngOnDestroy(): void {
		this.onCreateComplete.unsubscribe();
	}

	init(entities: SiteEntity[]): void {
		this.parents = entities;
		this.component = this.parents[this.parents.length - 1];
	}

	close(): void {
		this.bsModalRef.hide();
	}


	isValid(): boolean {
		if ((this.metadata.uav == null || this.metadata.uav.length === 0)) {
			return false;
		}

		if ((this.metadata.sensor == null || this.metadata.sensor.length === 0)) {
			return false;
		}

		if ((this.metadata.collectionDate == null || this.metadata.collectionDate.length === 0)) {
			return false;
		}

		return true;
	}

	handleCreate(): void {
		let payload = {
			productGroupName: this.productGroupName,
			component: this.component.id,
			metadata: this.metadata
		};

		this.service.createStandaloneProductGroup(payload).then(resp => {
			this.onCreateComplete.next(resp.oid);
			this.bsModalRef.hide();
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	getMetadataPage(): any {
		return { selection: this.metadata };
	}

	error(err: any): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}

}
