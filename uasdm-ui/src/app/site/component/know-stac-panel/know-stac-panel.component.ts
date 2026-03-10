///
///
///

import { Component, OnInit, OnDestroy, Output, EventEmitter, Input, ViewEncapsulation, inject } from '@angular/core';

import { StacCollection, StacItem, StacLink, StacProperty, ToggleableLayer, ToggleableLayerType } from '@site/model/layer';
import { KnowStacService } from '@site/service/know-stac.service';
import { LngLatBounds } from 'maplibre-gl';
import { environment } from 'src/environments/environment';
import { NgIf, NgFor, NgSwitch, NgSwitchCase, NgSwitchDefault, KeyValuePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BooleanFieldComponent } from '@shared/component/boolean-field/boolean-field.component';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { getCollection, MapActions } from 'src/app/state/map.state';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import * as lodash from 'lodash';


@Component({
	selector: 'know-stac-panel',
	templateUrl: './know-stac-panel.component.html',
	styleUrls: [],
	standalone: true,
	imports: [NgIf, NgFor, FormsModule, BooleanFieldComponent, NgSwitch, NgSwitchCase, NgSwitchDefault, KeyValuePipe]
})
export class KnowStacPanelComponent implements OnInit, OnDestroy {

	private store = inject(Store);

	collection$: Observable<StacCollection> = this.store.select(getCollection);

	@Output() propertiesChange: EventEmitter<StacProperty[]> = new EventEmitter<StacProperty[]>();

	@Output() onViewExtent: EventEmitter<StacLink> = new EventEmitter<StacLink>();

	@Output() close: EventEmitter<void> = new EventEmitter<void>();

	collection: StacCollection;

	@Input() bounds: LngLatBounds = null;

	@Input() properties: StacProperty[] = null;


	visible: boolean = false;

	context: string;

	constructor(private service: KnowStacService) {
		this.context = environment.apiUrl;

		this.collection$.pipe(takeUntilDestroyed()).subscribe((collection) => this.collection = lodash.cloneDeep(collection));
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
		this.store.dispatch(MapActions.setCollection({ collection: null }));

		// this.collectionChange.emit({ visible: false, collection: null });
	}

	handleSearch(): void {
		const bbox = this.bounds.toArray().flat();

		this.service.search(bbox).then(collection => {

			this.store.dispatch(MapActions.setCollection({ collection }));

			// this.collectionChange.emit({ visible: this.visible, collection });
		})
	}

	handleCardToggle(event, link: StacLink): void {
		event.stopPropagation();


		if (!link.open && link.item == null) {
			this.service.item(link.href).then(item => {

				item.enabled = false;

				if (item.assets['thumbnail'] != null) {
					item.thumbnail = item.assets['thumbnail'].href;
				}
				else if (item.assets['overview'] != null) {
					item.thumbnail = item.assets['overview'].href;
				}

				this.store.dispatch(MapActions.toggleLinkItem({ link, item }));
			})
		}
		else {
			this.store.dispatch(MapActions.toggleLinkItem({ link }));
		}
	}

	handleToggleItem(link: StacLink): void {
		const item = link.item;

		item.enabled = !item.enabled;

		if (item.enabled) {

			// Create the map layer from the StacItem
			const layer: ToggleableLayer = {
				id: item.id,
				type: ToggleableLayerType.KNOWSTAC,
				layerName: item.properties.title,
				active: true,
				item: item,
			};

			this.store.dispatch(MapActions.addMapLayer({ layer }));
		}
		else {
			this.store.dispatch(MapActions.removeMapLayer({ id: item.id }));
		}

		this.store.dispatch(MapActions.setLinkItem({ link, item }));
	}


	handleGotoExtent(event, link: StacLink): void {
		event.stopPropagation();

		this.onViewExtent.emit(link);
	}

	handleToggleVisibility(): void {
		this.store.dispatch(MapActions.toggleCollectionVisibility());
	}
}
