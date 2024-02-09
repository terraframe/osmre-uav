///
///
///

import { Component } from '@angular/core';
import { environment } from 'src/environments/environment';

@Component({
	selector: 'processing-report',
	templateUrl: './processing-report.component.html'
})
export class ProcessingReportComponent {

    date: any;

	constructor() { }
	
	generate(): void {
		var dateArg = this.date ? "?date=" + this.date : "";
		
		window.location.href = environment.apiUrl + "/api/processing-report/generate" + dateArg;
	}

}