///
///
///

import { Component, OnInit, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';

import { ProductDocument, ImageSet, SiteEntity } from '@site/model/management';

import { BasicConfirmModalComponent } from '@shared/component';
import EnvironmentUtil from '@core/utility/environment-util';
import { ModalTypes } from '@shared/model/modal';
import { ImageSetService } from '@site/service/image-set.service';
import { NgClass, NgFor, NgIf } from '@angular/common';
import { BsDropdownDirective, BsDropdownMenuDirective, BsDropdownToggleDirective } from 'ngx-bootstrap/dropdown';

@Component({
	standalone: true,
	selector: 'image-set-page',
	templateUrl: './image-set-page.component.html',
	styleUrls: [],
	providers: [],
	imports: [NgIf, NgFor, NgClass, BsDropdownDirective, BsDropdownToggleDirective, BsDropdownMenuDirective]
})
export class ImageSetPageComponent implements OnInit, OnDestroy {

	@Input() entity: SiteEntity;
	@Input() processRunning: boolean;

	@Output() onError = new EventEmitter<HttpErrorResponse>();
	@Output() onRemove = new EventEmitter<ImageSet>();
	@Output() onUpdate = new EventEmitter<ImageSet>();

	loading = false;

	@Input() sets: ImageSet[] = [];

	context: string;

	constructor(
		private service: ImageSetService,
		private modalService: BsModalService
	) {
		this.context = EnvironmentUtil.getApiUrl();
	}

	ngOnInit(): void {

	}

	ngOnDestroy(): void {
	}


	handleRemove(set: ImageSet, file: ProductDocument): void {

		const modal = this.modalService.show(BasicConfirmModalComponent, {
			animated: true,
			backdrop: true, class: 'modal-xl',
			ignoreBackdropClick: true,
		});
		modal.content.message = 'Do you want to remove the file [' + file.name + '] from the set? This action cannot be undone.';
		modal.content.type = ModalTypes.danger;
		modal.content.submitText = 'Delete';

		modal.content.onConfirm.subscribe(() => {

			this.service.removeImage(set.id, file.id)
				.then(s => this.onUpdate.emit(s))
				.catch((err: HttpErrorResponse) => {
					this.error(err);
				});
		});
	}

	handleRemoveImageSet(set: ImageSet): void {

		const modal = this.modalService.show(BasicConfirmModalComponent, {
			animated: true,
			backdrop: true,
			class: 'modal-xl',
			ignoreBackdropClick: true,
		});
		modal.content.message = 'Do you want to delete the image set [' + set.name + ']? This action cannot be undone.';
		modal.content.type = ModalTypes.danger;
		modal.content.submitText = 'Delete';

		modal.content.onConfirm.subscribe(() => {
			this.service.remove(set.id)
				.then(() => {
					this.onRemove.emit(set);
				})
				.catch((err: HttpErrorResponse) => {
					this.error(err);
				});
		});
	}

	error(err: HttpErrorResponse): void {
		this.onError.emit(err);
	}
}
