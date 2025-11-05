///
///
///

import { Component, OnInit, Input, OnDestroy, Inject, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';


import { Filter, projectTypes, SiteEntity } from '@site/model/management';

import {
	fadeInOnEnterAnimation,
	fadeOutOnLeaveAnimation,
	slideInLeftOnEnterAnimation,
	slideInRightOnEnterAnimation,
} from 'angular-animations';
import { Observable, Subject } from 'rxjs';
import { SensorService } from '@site/service/sensor.service';
import { PlatformService } from '@site/service/platform.service';
import { UAVService } from '@site/service/uav.service';

@Component({
	standalone: false,
  selector: 'filter-modal',
	templateUrl: './filter-modal.component.html',
	styleUrls: [],
	animations: [
		fadeInOnEnterAnimation(),
		fadeOutOnLeaveAnimation(),
		slideInLeftOnEnterAnimation(),
		slideInRightOnEnterAnimation(),
	]
})
export class FilterModalComponent implements OnInit, OnDestroy {

	filter: Filter;

	bureaus: { value: string, label: string }[];

	projectTypes: string[] = projectTypes;

	public onFilterChange: Subject<Filter>;

	/* 
	 * Datasource to get search responses
	 */
	sensorSource: Observable<any>;

	/*
	 * Datasource to get search responses
	 */
	platformSource: Observable<any>;

	/*
	 * Datasource to get search responses
	 */
	uavSource: Observable<any>;



	constructor(
		public bsModalRef: BsModalRef,
		public sensorService: SensorService,
		public platformService: PlatformService,
		public uavService: UAVService) {

		this.sensorSource = new Observable((observer: any) => {
			this.sensorService.search(this.filter.sensor).then(results => {
				observer.next(results.map(f => f.name));
			});
		});

		this.platformSource = new Observable((observer: any) => {
			this.platformService.search(this.filter.platform).then(results => {
				observer.next(results.map(f => f.name));
			});
		});

		this.uavSource = new Observable((observer: any) => {
			this.uavService.search(this.filter.uav, 'faaNumber').then(results => {
				observer.next(results.map(f => f.faaNumber));
			});
		});
	}


	ngOnInit(): void {
		this.onFilterChange = new Subject();
	}

	ngOnDestroy(): void {
		this.onFilterChange.unsubscribe();
	}

	init(filter: Filter, bureaus: { value: string, label: string }[]): void {
		this.filter = filter;
		this.bureaus = bureaus;
	}

	close(): void {
		this.onFilterChange.next(this.filter);

		this.bsModalRef.hide();
	}

}
