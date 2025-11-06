///
///
///

import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { ErrorHandler } from '@shared/component';

import { Classification, ClassificationComponentMetadata } from '@site/model/classification';
import { ClassificationService } from '@site/service/classification.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
	standalone: false,
	selector: 'classification',
	templateUrl: './classification.component.html',
	styleUrls: []
})
export class ClassificationComponent implements OnInit {

	metadata: ClassificationComponentMetadata;
	classification: Classification;
	original: Classification;
	newInstance: boolean = false;

	message: string = null;

	mode: string = 'READ';

	constructor(private service: ClassificationService, private route: ActivatedRoute, private router: Router) { }

	ngOnInit(): void {
		this.route.data.subscribe(data => {
			this.metadata = data as ClassificationComponentMetadata;

			const oid = this.route.snapshot.params['oid'];

			if (oid === '__NEW__') {
				this.service.newInstance(this.metadata.baseUrl).then((classification: Classification) => {
					this.classification = classification;
					this.newInstance = true;
					this.mode = 'WRITE';
				});
			}
			else {
				this.service.get(this.metadata.baseUrl, oid).then((classification: Classification) => {
					this.classification = classification;
					this.original = JSON.parse(JSON.stringify(this.classification));
				});
			}
		})
	}

	handleOnSubmit(): void {
		this.message = null;

		this.service.apply(this.metadata.baseUrl, this.classification).then(data => {
			this.classification = data;
			this.mode = 'READ';

			if (this.newInstance) {

				console.log('Navigate', this.metadata.route)

				this.router.navigate(['/site/' + this.metadata.route, data.oid]);
				this.newInstance = false;
				this.original = data;
			}
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	handleOnCancel(): void {
		this.message = null;

		this.classification = JSON.parse(JSON.stringify(this.original));
		this.mode = 'READ';
	}

	handleOnEdit(): void {
		this.mode = 'WRITE';
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}

}
