///
///
///

import { Component, OnInit, OnDestroy, Output, EventEmitter, Input } from '@angular/core';

import { StacItem, StacProperty } from '@site/model/layer';
import { KnowStacService } from '@site/service/know-stac.service';
import { environment } from 'src/environments/environment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { NgIf, NgFor, NgSwitch, NgSwitchCase, NgSwitchDefault, KeyValuePipe } from '@angular/common';


@Component({
    selector: 'know-stac-modal',
    templateUrl: './know-stac-modal.component.html',
    styleUrls: [],
    standalone: true,
    imports: [NgIf, NgFor, NgSwitch, NgSwitchCase, NgSwitchDefault, KeyValuePipe]
})
export class KnowStacModalComponent implements OnInit, OnDestroy {



	@Input() item: StacItem;

	@Input() properties: StacProperty[] = null;


	context: string;

	constructor(private service: KnowStacService, public bsModalRef: BsModalRef, private modalService: BsModalService) {
		this.context = environment.apiUrl;
	}

	init(item: StacItem, properties: StacProperty[]): void {
		this.item = item;
		this.properties = properties;

		if (this.properties == null) {
			this.service.properties().then(props => {
				this.properties = props;
			});
		}
	}

	ngOnInit(): void {

	}

	ngOnDestroy(): void {
	}
}
