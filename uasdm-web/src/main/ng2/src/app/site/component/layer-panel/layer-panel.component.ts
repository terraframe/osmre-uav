import { Component, OnInit, OnDestroy, Output, EventEmitter, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { v4 as uuidv4 } from 'uuid';

import { ManagementService } from '@site/service/management.service';
import { Filter, StacAsset, StacItem, StacLayer } from '@site/model/layer';
import { PageResult } from '@shared/model/page';

const enum VIEW_MODE {
	FORM = 0,
	RESULTS = 1
}

@Component({
	selector: 'layer-panel',
	templateUrl: './layer-panel.component.html',
	styleUrls: []
})
export class LayerPanelComponent implements OnInit, OnDestroy {

	/* 
	 * Datasource to get search responses
	 */
	dataSource: Observable<any>;

	/* 
	 * Model for text being searched
	 */
	search: string = "";

	/*
	 * Called on confirm
	 */
	@Output() confirm: EventEmitter<StacLayer> = new EventEmitter<StacLayer>();

	@Output() cancel: EventEmitter<void> = new EventEmitter<void>();

	@Input() layer: StacLayer;

	page: PageResult<StacItem> = null;

	viewMode: number = VIEW_MODE.FORM;

	constructor(private service: ManagementService) {
		this.dataSource = new Observable((observer: any) => {

			const filters = this.getFilters();

			this.service.getTotals(this.search, filters).then(results => {
				results.forEach(result => result.text = this.search);

				observer.next(results);
			});
		});
	}

	ngOnInit(): void {
		if (this.layer == null) {
			this.layer = {
				id: uuidv4(),
				layerName: "",
				startDate: "",
				endDate: "",
				filters: [],
				items: []
			}
		}
	}

	ngOnDestroy(): void {
	}

	getFilters(): Filter[] {
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

		return filters;
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

	handlePageChange(pageNumber: number): void {
		const filters = this.getFilters();

		this.service.getStacItems(filters, 20, pageNumber).then(page => {
			this.page = page;
		});
	}

	handleToggleAsset(item: StacItem, key: string): void {
		item.assets[key].selected = !item.assets[key].selected;

		const index = this.layer.items.findIndex(i => i.id === item.id);

		if (index === -1 && item.assets[key].selected) {
			this.layer.items.push(item);
		}
		else if (index !== -1 && !item.assets[key].selected) {
			this.layer.items = this.layer.items.filter(f => f.id !== item.id);
		}

		console.log(item);
	}

	handleSubmit(): void {
		if (this.viewMode === VIEW_MODE.FORM) {
			this.handlePageChange(1);

			this.viewMode = VIEW_MODE.RESULTS;
		}
		else if (this.viewMode === VIEW_MODE.RESULTS) {
			this.confirm.emit(this.layer);
		}
	}

	close(): void {
		this.cancel.emit();
	}
}
