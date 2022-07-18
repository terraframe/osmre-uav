import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Observable, Subject } from 'rxjs';
import { v4 as uuidv4 } from 'uuid';

import { ErrorHandler } from '@shared/component';
import { ManagementService } from '@site/service/management.service';
import { Filter, StacLayer } from '@site/model/layer';


@Component({
	selector: 'layer-modal',
	templateUrl: './layer-modal.component.html',
	styleUrls: []
})
export class LayerModalComponent implements OnInit, OnDestroy {

	message: string = null;

	/* 
	 * Datasource to get search responses
	 */
	dataSource: Observable<any>;

	/* 
	 * Model for text being searched
	 */
	search: string = "";

	layer: StacLayer;

	/*
	 * Called on confirm
	 */
	public onConfirm: Subject<StacLayer>;


	constructor(public bsModalRef: BsModalRef, private service: ManagementService) {
		this.dataSource = new Observable((observer: any) => {

			const filters = [...this.layer.filters];

			if (this.layer.startDate.length > 0 || this.layer.endDate.length > 0) {
				const filter: Filter = {
					id: uuidv4(),
					label: "Date",
					field: "datetime",
				};

				if (this.layer.startDate.length > 0) {
					filter.startDate = this.layer.startDate;
				}

				if (this.layer.endDate.length > 0) {
					filter.endDate = this.layer.endDate;
				}

				filters.push(filter);
			}

			this.service.getTotals(this.search, filters).then(results => {
				results.forEach(result => result.text = this.search);

				observer.next(results);
			});
		});

		this.layer = {
			id: uuidv4(),
			layerName: "",
			startDate: "",
			endDate: "",
			filters: []
		}
	}

	ngOnInit(): void {
		this.onConfirm = new Subject();
	}

	ngOnDestroy(): void {
		this.onConfirm.unsubscribe();
	}

	init(layer: StacLayer): void {
		if (layer != null) {
			this.layer = layer;
		}
	}

	handleClick($event: any): void {
		this.layer.filters.push({
			id: uuidv4(),
			label: $event.item.label,
			field: $event.item.key,
			value: $event.item.text
		});

		this.search = "";
	}

	handleRemove(filter: Filter): void {
		this.layer.filters = this.layer.filters.filter(f => f.id !== filter.id);
	}

	close(): void {
		this.bsModalRef.hide();
	}


	handleSubmit(): void {
		this.onConfirm.next(this.layer);
		this.bsModalRef.hide();
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}
}
