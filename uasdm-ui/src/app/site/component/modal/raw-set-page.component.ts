///
///
///

import { Component, OnInit, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';

import { ProductDocument, RawSet, SiteEntity } from '@site/model/management';

import { WebSocketSubject } from 'rxjs/webSocket';
import { BasicConfirmModalComponent } from '@shared/component';
import EnvironmentUtil from '@core/utility/environment-util';
import { ModalTypes } from '@shared/model/modal';
import { RawSetService } from '@site/service/raw-set.service';

@Component({
	standalone: false,
	selector: 'raw-set-page',
	templateUrl: './raw-set-page.component.html',
	styleUrls: [],
	providers: []
})
export class RawSetPageComponent implements OnInit, OnDestroy {

	@Input() entity: SiteEntity;
	@Input() processRunning: boolean;

	@Output() onError = new EventEmitter<HttpErrorResponse>();

	loading = false;

	sets: RawSet[];

	context: string;

	constructor(
		private service: RawSetService,
		private modalService: BsModalService
	) {
		this.context = EnvironmentUtil.getApiUrl();
	}

	ngOnInit(): void {

		this.loading = true;
		this.service.list(this.entity.id).then(sets => {

			this.loading = false;

			this.sets = sets;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	ngOnDestroy(): void {
	}


	handleRemove(set: RawSet, file: ProductDocument): void {

		const modal = this.modalService.show(BasicConfirmModalComponent, {
			animated: true,
			backdrop: true, class: 'modal-xl',
			ignoreBackdropClick: true,
		});
		modal.content.message = 'Do you want to remove the file [' + file.name + '] from the set? This action cannot be undone.';
		modal.content.type = ModalTypes.danger;
		modal.content.submitText = 'Delete';

		modal.content.onConfirm.subscribe(() => {
			// this.service.removeArtifacts(this.entity.id, name, section.folder).then(artifacts => {

			// 	// TODO: Handle refresh
			// 	//				this.artifacts = artifacts;
			// }).catch((err: HttpErrorResponse) => {
			// 	this.error(err);
			// });
		});
	}

	handleRemoveRawSet(set: RawSet): void {

		const modal = this.modalService.show(BasicConfirmModalComponent, {
			animated: true,
			backdrop: true,
			class: 'modal-xl',
			ignoreBackdropClick: true,
		});
		modal.content.message = 'Do you want to delete the raw set [' + set.name + ']? This action cannot be undone.';
		modal.content.type = ModalTypes.danger;
		modal.content.submitText = 'Delete';

		modal.content.onConfirm.subscribe(() => {
			this.service.remove(set.id)
				.then(() => {
					const index = this.sets.findIndex(s => s.id === set.id);

					if (index != -1) {
						this.sets.splice(index);
					}
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
