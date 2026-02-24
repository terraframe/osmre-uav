///
///
///

import { Component } from '@angular/core';
import { RouterModule, RouterOutlet } from '@angular/router';
import { LoadingBarComponent } from '@shared/component';
import { environment } from 'src/environments/environment';

@Component({

  standalone: true,
   selector: 'uasdm-app',
  templateUrl: './app.component.html',
  styleUrls: [],
  imports: [RouterOutlet, RouterModule, LoadingBarComponent], // Needed for routing
})
export class AppComponent {

  favIcon: HTMLLinkElement = document.querySelector('#appIcon');

  constructor() {
    this.favIcon.href = environment.apiUrl + '/api/logo/view?oid=logo';
  }

}
