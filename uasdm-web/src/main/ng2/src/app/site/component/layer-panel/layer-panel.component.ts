import { Component, OnInit, OnDestroy, Output, EventEmitter, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { v4 as uuidv4 } from 'uuid';

import { ManagementService } from '@site/service/management.service';
import { Filter, StacItem, StacLayer } from '@site/model/layer';
import { PageResult } from '@shared/model/page';

const enum VIEW_MODE {
	FORM = 0,
	RESULTS = 1
}

declare var acp: string;

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

	@Output() confirm: EventEmitter<StacLayer> = new EventEmitter<StacLayer>();

	@Output() cancel: EventEmitter<void> = new EventEmitter<void>();

	@Input() layer: StacLayer;

	page: PageResult<StacItem> = null;

	viewMode: number = VIEW_MODE.FORM;

	thumbnails: any = {};
	context: string;

	constructor(private service: ManagementService) {
		this.context = acp;

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
		this.thumbnails = {};
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

	onPageChange(pageNumber: number): void {
		const filters = this.getFilters();

		this.service.getStacItems(filters, 20, pageNumber).then(page => {
			this.thumbnails = {};
			this.page = page;

			// Replace any incoming stac item with the existing definition
			// if that stac item has already been selected
			this.layer.items.forEach(item => {
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

		const index = this.layer.items.findIndex(i => i.id === item.id);

		if (index === -1 && item.enabled) {
			this.layer.items.push(item);
		}
		else if (index !== -1 && !item.enabled) {
			this.layer.items = this.layer.items.filter(f => f.id !== item.id);
		}
	}

	handleSubmit(): void {
		if (this.viewMode === VIEW_MODE.FORM) {
			this.onPageChange(1);

			this.viewMode = VIEW_MODE.RESULTS;
		}
		else if (this.viewMode === VIEW_MODE.RESULTS) {
			this.confirm.emit(this.layer);
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
