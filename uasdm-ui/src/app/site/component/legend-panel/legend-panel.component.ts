///
///
///

import { Component, OnInit, OnDestroy, Output, EventEmitter, Input, OnChanges, SimpleChanges } from '@angular/core';
import { LayerColor, StacCollection } from '@site/model/layer';
import { CollectionProductView } from '@site/model/management';
import { NgIf } from '@angular/common';

@Component({
    selector: 'legend-panel',
    templateUrl: './legend-panel.component.html',
    styleUrls: [],
    standalone: true,
    imports: [NgIf]
})
export class LegendPanelComponent implements OnChanges, OnInit {


	@Input() collection: StacCollection = null;

	@Input() views: CollectionProductView[] = [];

	open: boolean = false;

	loaded: boolean = false;

	hasOpened: boolean = false;

	readonly LayerColor = LayerColor;


	ngOnChanges(changes: SimpleChanges): void {
		if (this.loaded && !this.hasOpened && (changes.collection != null || changes.views != null)) {

			this.open = true;
			this.hasOpened = true;
		}
	}

	ngOnInit(): void {
		this.loaded = true;
	}

	handleClick(): void {
		this.open = !this.open;
	}
}
