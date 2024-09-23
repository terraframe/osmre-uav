///
///
///

import { Component, OnInit, OnDestroy, Output, EventEmitter, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { v4 as uuid } from "uuid";

import { ManagementService } from '@site/service/management.service';
import { Criteria, Filter, StacItem, ToggleableLayer, ToggleableLayerType } from '@site/model/layer';
import { PageResult } from '@shared/model/page';
import { LngLatBounds } from 'mapbox-gl';
import { environment } from 'src/environments/environment';

const enum VIEW_MODE {
	FORM = 0,
	RESULTS = 1
}

@Component({
	selector: 'stac-item-panel',
	templateUrl: './stac-item-panel.component.html',
	styleUrls: []
})
export class StacItemPanelComponent implements OnInit, OnDestroy {

	/* 
	 * Datasource to get search responses
	 */
	dataSource: Observable<any>;

	/* 
	 * Model for text being searched
	 */
	search: string = "";

	@Output() confirm: EventEmitter<ToggleableLayer> = new EventEmitter<ToggleableLayer>();

	@Output() cancel: EventEmitter<void> = new EventEmitter<void>();

	@Input() layer: ToggleableLayer;

	@Input() bounds: LngLatBounds = null;

	page: PageResult<StacItem> = null;

	viewMode: number = VIEW_MODE.FORM;

	thumbnails: any = {};

	context: string;

	readonly: boolean;

	constructor(private service: ManagementService) {
		this.context = environment.apiUrl;

		this.dataSource = new Observable((observer: any) => {

			const filters = this.getFilters();

			this.service.getTotals(this.search, filters).then(results => {
				results.forEach(result => result.text = this.search);

				observer.next(results);
			});
		});
	}

	ngOnInit(): void {
		this.readonly = (this.layer != null);
		if (this.layer == null) {
			this.layer = {
				id: uuid(),
				type: ToggleableLayerType.STAC,
				layerName: "",
				active: true,
				item: {
					startDate: "",
					endDate: "",
					filters: [],
					items: [],
				}
			}
		}
	}

	ngOnDestroy(): void {
		this.thumbnails = {};
	}

	getFilters(): Filter[] {
		const filters = [...this.layer.item.filters];

		if (this.layer.item.startDate.length > 0 || this.layer.item.endDate.length > 0) {
			const filter: Filter = {
				id: uuid(),
				label: "Date",
				field: "datetime",
			};

			if (this.layer.item.startDate.length > 0) {
				filter.startDate = this.layer.item.startDate;
			}

			if (this.layer.item.endDate.length > 0) {
				filter.endDate = this.layer.item.endDate;
			}

			filters.push(filter);
		}

		return filters;
	}

	handleClick($event: any): void {
		this.layer.item.filters.push({
			id: uuid(),
			label: $event.item.label,
			field: $event.item.key,
			value: $event.item.text
		});

		this.search = "";
	}

	handleRemove(filter: Filter): void {
		this.layer.item.filters = this.layer.item.filters.filter(f => f.id !== filter.id);
	}

	onPageChange(pageNumber: number): void {
		const filters = this.getFilters();

		const criteria: Criteria = {};

		if (filters != null && filters.length > 0) {
			criteria.must = filters;
		}

		if (this.bounds != null) {
			criteria.should = [{
				field: "bounds",
				id: uuid(),
				label: "Bounds",
				value: this.bounds
			}];
		}

		this.service.getStacItems(criteria, 20, pageNumber).then(page => {
			this.thumbnails = {};
			this.page = page;

			// Replace any incoming stac item with the existing definition
			// if that stac item has already been selected
			this.layer.item.items.forEach(item => {
				const index = this.page.resultSet.findIndex(i => item.id === i.id);

				if (index !== -1) {
					this.page.resultSet[index] = item;
				}
			});

			// Get the thumbnail of each item
			this.page.resultSet.forEach(item => {
				this.getThumbnail(item);
			});

			// Assign the default asset for all stac items
			this.page.resultSet.forEach(item => {
				if (item.asset == null) {
					if (item.assets['odm_orthophoto.cog'] != null) {
						item.asset = 'odm_orthophoto.cog';
					}
					else {
						const keys = Object.keys(item.assets);

						keys.forEach(key => {
							if (item.assets[key].type === "image/tiff; application=geotiff; profile=cloud-optimized") {
								item.asset = key;
							}
						});
					}
				}
			});
		});
	}

	handleToggleItem(item: StacItem): void {
		item.enabled = !item.enabled;

		const index = this.layer.item.items.findIndex(i => i.id === item.id);

		if (index === -1 && item.enabled) {
			this.layer.item.items.push(item);
		}
		else if (index !== -1 && !item.enabled) {
			this.layer.item.items = this.layer.item.items.filter(f => f.id !== item.id);
		}
	}

	handleSubmit(): void {
		if (this.viewMode === VIEW_MODE.FORM) {
			this.onPageChange(1);

			this.viewMode = VIEW_MODE.RESULTS;
		}
		else if (this.viewMode === VIEW_MODE.RESULTS) {
			this.confirm.emit(this.layer);
			this.close();
		}
	}

	close(): void {
		this.cancel.emit();
	}

	getThumbnail(item: StacItem): void {
		if (item.assets["thumbnail"] != null) {
			const key = item.assets["thumbnail"].href;

			this.service.downloadFile(key, false).then(blob => {
				this.createImageFromBlob(blob, item);
			}, error => {
				console.log(error);
			});
		}
	}

	createImageFromBlob(image: Blob, item: StacItem) {
		let reader = new FileReader();
		reader.addEventListener("load", () => {
			// this.imageToShow = reader.result;
			this.thumbnails[item.id] = reader.result;
		}, false);

		if (image) {
			reader.readAsDataURL(image);
		}
	}

}
