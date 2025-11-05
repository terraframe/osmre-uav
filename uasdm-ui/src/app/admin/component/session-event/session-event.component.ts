///
///
///

import { Component, OnInit } from '@angular/core';

import { PageResult } from '@shared/model/page';

import { SessionEvent } from '../../model/session-event';
import { SessionEventService } from '../../service/session-event.service';

import { environment } from 'src/environments/environment';

@Component({
	standalone: false,
  selector: 'session-events',
	templateUrl: './session-event.component.html',
	styleUrls: []
})
export class SessionEventComponent implements OnInit {
	res: PageResult<SessionEvent> = {
		resultSet: [],
		count: 0,
		pageNumber: 1,
		pageSize: 20
	};
	p: number = 1;

	constructor(private service: SessionEventService) { }

	ngOnInit(): void {
		this.service.page(this.p).then(res => {
			this.res = res;
		});
	}

	onPageChange(pageNumber: number): void {
		this.service.page(pageNumber).then(res => {
			this.res = res;

			this.p = pageNumber;
		});
	}
	
	onExport(): void {
		window.location.href = environment.apiUrl + "/api/session-event/export";
	}
}
