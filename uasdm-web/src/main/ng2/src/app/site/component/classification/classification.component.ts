import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { ErrorHandler } from '@shared/component';

import { Classification, ComponentMetadata } from '@site/model/classification';

@Component({
	selector: 'classification',
	templateUrl: './classification.component.html',
	styleUrls: []
})
export class ClassificationComponent implements OnInit {

	metadata: ComponentMetadata;
	classification: Classification;
	newInstance: boolean = false;

	message: string = null;

	/*
	 * Observable subject for TreeNode changes.  Called when create is successful 
	 */
	public onClassificationChange: Subject<Classification>;

	constructor(public bsModalRef: BsModalRef) { }

	ngOnInit(): void {
		this.onClassificationChange = new Subject();
	}

	init(metadata: ComponentMetadata, classification: Classification, newInstance: boolean) {
		this.metadata = metadata;
		this.classification = classification;
		this.newInstance = newInstance;
	}

	handleOnSubmit(): void {
		this.message = null;

		this.metadata.service.apply(this.classification).then(data => {
			this.onClassificationChange.next(data);
			this.bsModalRef.hide();
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	handleOnCancel(): void {
		this.message = null;
		this.bsModalRef.hide();
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}

}
