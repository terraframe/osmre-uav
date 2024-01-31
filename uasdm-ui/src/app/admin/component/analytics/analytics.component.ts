import { Component } from '@angular/core';
import { environment } from 'src/environments/environment';

@Component({
	selector: 'analytics',
	templateUrl: './analytics.component.html'
})
export class AnalyticsComponent {

	constructor() { }
	
	generate(): void {
		window.location.href = environment.apiUrl + "/api/analytics/generate";
	}

}