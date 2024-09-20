///
///
///

import { Component, OnInit, OnDestroy, Output, EventEmitter, Input } from '@angular/core';

import { StacCollection, StacItem, StacLink, StacProperty } from '@site/model/layer';
import { LngLatBounds, Map } from 'mapbox-gl';
import { KnowStacService } from '@site/service/know-stac.service';
import { environment } from 'src/environments/environment';


@Component({
	selector: 'know-stac-panel',
	templateUrl: './know-stac-panel.component.html',
	styleUrls: []
})
export class KnowStacPanelComponent implements OnInit, OnDestroy {


	@Output() propertiesChange: EventEmitter<StacProperty[]> = new EventEmitter<StacProperty[]>();

	@Output() collectionChange: EventEmitter<StacCollection> = new EventEmitter<StacCollection>();

	@Output() onViewExtent: EventEmitter<StacLink> = new EventEmitter<StacLink>();

	@Output() onToggleMapItem: EventEmitter<StacItem> = new EventEmitter<StacItem>();

	@Output() close: EventEmitter<void> = new EventEmitter<void>();

	@Input() collection: StacCollection;

	@Input() bounds: LngLatBounds = null;

	@Input() properties: StacProperty[] = null;


	context: string;

	constructor(private service: KnowStacService) {
		this.context = environment.apiUrl;
	}

	ngOnInit(): void {
		if (this.properties == null) {
			this.service.properties().then(props => {
				this.propertiesChange.emit(props);
			});
		}
	}

	ngOnDestroy(): void {
	}

	handleClose(): void {
		this.close.emit();
	}

	handleClear(): void {
		this.collectionChange.emit();
	}

	handleSearch(): void {
		const bbox = this.bounds.toArray().flat();

		this.service.search(bbox).then(collection => {
			this.collectionChange.emit(collection);
		})
	}

	handleCardToggle(link: StacLink): void {
		link.open = !link.open;

		if (link.open && link.item == null) {
			this.service.item(link.href).then(item => {
				item.enabled = false;

				if (item.assets.thumbnail != null) {
					item.thumbnail = item.assets.thumbnail.href;
				}
				else if (item.assets['thumbnail-hd'] != null) {
					item.thumbnail = item.assets['thumbnail-hd'].href;
				}

				link.item = item;
			})
		}
	}

	handleToggleItem(item: StacItem): void {
		item.enabled = !item.enabled;

		this.onToggleMapItem.emit(item);
	}

	handleGotoExtent(link: StacLink): void {
		this.onViewExtent.emit(link);
	}

}
