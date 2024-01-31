import { Component } from '@angular/core';
import { environment } from 'src/environments/environment';

@Component({
	selector: 'analytics',
	templateUrl: './analytics.component.html'
})
export class AnalyticsComponent {

    date: any;

	constructor() { }
	
	generate(): void {
		var dateArg = this.date ? "?date=" + this.date : "";
		
		window.location.href = environment.apiUrl + "/api/analytics/generate" + dateArg;
	}

}