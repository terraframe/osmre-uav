///
///
///

import { Component, OnInit, OnDestroy, Output, EventEmitter, Input, ViewEncapsulation } from '@angular/core';

import { StacCollection, StacItem, StacLink, StacProperty } from '@site/model/layer';
import { KnowStacService } from '@site/service/know-stac.service';
import { LngLatBounds } from 'maplibre-gl';
import { environment } from 'src/environments/environment';
import { NgIf, NgFor, NgSwitch, NgSwitchCase, NgSwitchDefault, KeyValuePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BooleanFieldComponent } from '@shared/component/boolean-field/boolean-field.component';


@Component({
	selector: 'know-stac-panel',
	templateUrl: './know-stac-panel.component.html',
	styleUrls: [],
	standalone: true,
	imports: [NgIf, NgFor, FormsModule, BooleanFieldComponent, NgSwitch, NgSwitchCase, NgSwitchDefault, KeyValuePipe]
})
export class KnowStacPanelComponent implements OnInit, OnDestroy {


	@Output() propertiesChange: EventEmitter<StacProperty[]> = new EventEmitter<StacProperty[]>();

	@Output() collectionChange: EventEmitter<{ visible: boolean, collection: StacCollection }> = new EventEmitter<{ visible: boolean, collection: StacCollection }>();

	@Output() onViewExtent: EventEmitter<StacLink> = new EventEmitter<StacLink>();

	@Output() onToggleMapItem: EventEmitter<StacItem> = new EventEmitter<StacItem>();

	@Output() onToggleVisibility: EventEmitter<boolean> = new EventEmitter<boolean>();

	@Output() close: EventEmitter<void> = new EventEmitter<void>();

	@Input() collection: StacCollection;

	@Input() bounds: LngLatBounds = null;

	@Input() properties: StacProperty[] = null;


	visible: boolean = false;

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
		this.collectionChange.emit({ visible: false, collection: null });
	}

	handleSearch(): void {
		const bbox = this.bounds.toArray().flat();

		this.service.search(bbox).then(collection => {
			this.collectionChange.emit({ visible: this.visible, collection });
		})
	}

	handleCardToggle(event, link: StacLink): void {
		event.stopPropagation();

		link.open = !link.open;

		if (link.open && link.item == null) {
			this.service.item(link.href).then(item => {
				item.enabled = false;

				if (item.assets['thumbnail'] != null) {
					item.thumbnail = item.assets['thumbnail'].href;
				}
				else if (item.assets['overview'] != null) {
					item.thumbnail = item.assets['overview'].href;
				}

				link.item = item;
			})
		}
	}

	handleToggleItem(item: StacItem): void {

		item.enabled = !item.enabled;

		this.onToggleMapItem.emit(item);
	}

	handleGotoExtent(event, link: StacLink): void {
		event.stopPropagation();

		this.onViewExtent.emit(link);
	}

	handleToggleVisibility(): void {
		this.onToggleVisibility.emit(this.visible);
	}
}
