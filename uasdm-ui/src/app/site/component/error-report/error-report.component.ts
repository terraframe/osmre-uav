import { Component } from '@angular/core';
import { environment } from 'src/environments/environment';

@Component({
	selector: 'error-report',
	templateUrl: './error-report.component.html'
})
export class ErrorReportComponent {

    date: any;

	constructor() { }
	
	generate(): void {
		var dateArg = this.date ? "?date=" + this.date : "";
		
		window.location.href = environment.apiUrl + "/api/analytics/generate" + dateArg;
	}

}