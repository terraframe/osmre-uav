///
///
///

import { Component } from '@angular/core';
import { environment } from 'src/environments/environment';

@Component({
	selector: 'processing-report',
	templateUrl: './processing-report.component.html',
	styleUrl: './processing-report.component.scss'
})
export class ProcessingReportComponent {

    date: string = new Date(new Date().setMonth(new Date().getMonth() - 6)).toISOString().split('T')[0];

	constructor() { }
	
	generate(): void {
		var dateArg = this.date ? "?date=" + this.date : "";
		
		window.location.href = environment.apiUrl + "/api/processing-report/generate" + dateArg;
	}

}