///
///
///

import { Component } from '@angular/core';
import { environment } from 'src/environments/environment';

@Component({

  standalone: false, selector: 'uasdm-app',
  templateUrl: './uasdm-app.component.html',
  styleUrls: [],
})
export class UasdmAppComponent {

  favIcon: HTMLLinkElement = document.querySelector('#appIcon');

  constructor() {
    this.favIcon.href = environment.apiUrl + '/api/logo/view?oid=logo';
  }

}
