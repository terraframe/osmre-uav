///
///
///

import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

import { Subject } from 'rxjs';

import { ErrorHandler } from '@shared/component';

import { NgxSpinnerService } from "ngx-spinner";
import { Sensor } from '@site/model/sensor';
import { Platform } from '@site/model/platform';
import { SiteEntity, Selection } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';
import { MetadataService } from '@site/service/metadata.service';

import { StepConfig } from '@shared/modal/step-indicator/modal-step-indicator'

import {
	fadeInOnEnterAnimation,
	fadeOutOnLeaveAnimation
} from 'angular-animations';
import { UploadModalComponent } from './upload-modal.component';

export class Page {
	index?: number;
	selection?: Selection;
	options?: SiteEntity[];
	type?: string
};

@Component({
	selector: 'create-collection-modal',
	templateUrl: './create-collection-modal.component.html',
	styleUrls: ['./upload-modal.component.css'],
	animations: [
		fadeInOnEnterAnimation(),
		fadeOutOnLeaveAnimation()
	]
})
export class CreateCollectionModalComponent implements OnInit, OnDestroy {
	message: string = "";

	disabled: boolean = false;

	/*
	 * List of hierarchies
	 */
	hierarchy: string[] = [];

	/*
	 * List of selections: One per hierarchy type
	 */
	selections: Selection[] = [];

	/*
	 * List of previous selection labels
	 */
	labels: string[] = [];

	/*
	 * List of pages
	 */
	pages: Page[] = [{
		index: 0,
		selection: null,
		options: [],
		type: 'FILE',
	}];

	/*
	 * Current page  
	 */
	hierarchyChange: boolean = false;
	
	CONSTANTS = {
        NEXT_OVERLAY: "create-collection-next"
    };

	/*
	 * Current page  
	 */
	page: Page = this.pages[0];

	sensors: Sensor[] = [];
	platforms: Platform[] = [];

	public onCreateComplete: Subject<any>;

	public onHierarchyChange: Subject<boolean>;

	modalStepConfig: StepConfig = {
		"steps": [
			{ "label": "Category", "active": true, "enabled": true },
			{ "label": "Final", "active": true, "enabled": false }
		]
	};

	constructor(private spinner: NgxSpinnerService, private service: ManagementService, private metadataService: MetadataService, public bsModalRef: BsModalRef) {
	}


	ngAfterViewInit() {

	}

	ngOnInit(): void {
		this.onCreateComplete = new Subject();
		this.onHierarchyChange = new Subject();

		// this.service.getMetadataOptions(null).then((options) => {
		// 	this.sensors = options.sensors;
		// 	this.platforms = options.platforms;
		// }).catch((err: HttpErrorResponse) => {
		// 	this.error(err);
		// });
	}

	ngOnDestroy(): void {
		this.onCreateComplete.unsubscribe();
		this.onHierarchyChange.unsubscribe();
	}

	init(entities: SiteEntity[]): void {
		this.hierarchy = this.metadataService.getHierarchy();
		this.selections = [];
		this.pages = [];

		for (let i = 0; i < this.hierarchy.length; i++) {
			const type = this.hierarchy[i];

			const index = entities.findIndex(entity => { return entity.type === type });

			if (index !== -1) {
				const entity = entities[index];

				this.selections.push({
					type: type,
					isNew: (i === (this.hierarchy.length - 1)),
					value: entity.id,
					label: entity.name
				});
			}
			else {
				this.selections.push({
					type: type,
					isNew: (i === (this.hierarchy.length - 1)),
					value: null,
					label: ''
				});
			}

			if (i > 0) {
				this.pages.push({
					index: (this.pages.length),
					selection: this.selections[i],
					options: [],
					type: 'CATEGORY'
				});
			}
		}

		this.labels.push(this.selections[0].label);

		this.pages.push({
			index: (this.pages.length),
			selection: null,
			options: [],
			type: 'SUMMARY'
		});

		this.page = this.pages[0];

		this.service.getChildren(this.selections[0].value).then(children => {
			this.pages[0].options = children.filter(child => {
				return child.type === this.pages[0].selection.type;
			});
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	close(): void {
		if (this.hierarchyChange) {
			this.onHierarchyChange.next(true);
		}

		this.bsModalRef.hide();
	}


	isPageValid(page: Page): boolean {
		if (page.type === 'CATEGORY') {
			if (page.selection != null) {
				if (page.selection.value != null && page.selection.value.length > 0) {
					return true;
				}
				else if (page.selection.label != null && page.selection.label.length > 0) {
					if (this.hasField('uav') && (page.selection.uav == null || page.selection.uav.length === 0)) {
						return false;
					}

					if (this.hasField('sensor') && (page.selection.sensor == null || page.selection.sensor.length === 0)) {
						return false;
					}

					if (this.hasField('collectionDate') && (page.selection.collectionDate == null || page.selection.collectionDate.length === 0)) {
						return false;
					}


					return true;
				}

				return false;
			}
		}

		return true;
	}

	updateCurrentPageLabel(): void {
		this.page.options.forEach(entity => {
			if (entity.id === this.page.selection.value) {
				this.page.selection.label = entity.name;
			}
		})
	}

	setIsNew(isNew: boolean): void {
		this.page.selection.isNew = isNew;

		if (isNew) {
			this.page.selection.value = null;
		}
		else {
			this.page.selection.label = null;
		}
	}

	handleCreate(): void {
		this.service.createCollection(this.selections).then(resp => {
			this.onCreateComplete.next(resp.oid);
			this.bsModalRef.hide();
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	handleNextPage(): void {

		if ((this.page.index + 1) < this.pages.length) {

			const nextPage = this.pages[this.page.index + 1];

			if (this.page.type === 'CATEGORY') {
				this.updateCurrentPageLabel();
				this.labels.push(this.page.selection.label);
			}

			if (nextPage.type === 'CATEGORY') {

				if (this.page.type === 'FILE') {
					this.page = nextPage;
				}
				else {
					if (!this.page.selection.isNew && this.page.selection.value != null && this.page.selection.value.length > 0) {

						this.spinner.show(this.CONSTANTS.NEXT_OVERLAY);
						this.service.getChildren(this.page.selection.value).then(children => {
							nextPage.options = children.filter(child => {
								return child.type === nextPage.selection.type;
							});

							this.page = nextPage;
							this.spinner.hide(this.CONSTANTS.NEXT_OVERLAY);
						}).catch((err: HttpErrorResponse) => {
							this.spinner.hide(this.CONSTANTS.NEXT_OVERLAY);
							this.error(err);
						});
					}
					else {
						this.page = nextPage;
					}
				}
			}
			else {
				this.page = nextPage;

				this.modalStepConfig = {
					"steps": [
						{ "label": "Category", "active": true, "enabled": false },
						{ "label": "Final", "active": true, "enabled": false }
					]
				};
			}
		}
	}

	handleBackPage(): void {

		if (this.page.index > 0) {

			const prevPage = this.pages[this.page.index - 1];

			if (prevPage.type === 'CATEGORY') {
				this.labels.splice(this.labels.length - 1, 1);

				this.modalStepConfig.steps.forEach(step => {
					if (step.label.toLowerCase() === "category" && step.enabled === false) {
						this.modalStepConfig = {
							"steps": [
								{ "label": "Category", "active": true, "enabled": true },
								{ "label": "Final", "active": true, "enabled": false }
							]
						};
					}
				})
			}

			this.page = prevPage;
		}
	}


	hasField(fieldName: string): boolean {
		return this.metadataService.hasExtraField(this.page.selection.type, fieldName);
	}	

	error(err: any): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}

}
