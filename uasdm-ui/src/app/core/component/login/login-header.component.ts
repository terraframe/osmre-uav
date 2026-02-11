///
///
///

import { Component } from '@angular/core';
import EnvironmentUtil from '@core/utility/environment-util';

@Component({
    standalone: true,
    selector: 'login-header',
    templateUrl: './login-header.component.html',
    styleUrls: []
})
export class LoginHeaderComponent {
  context:string;

  constructor() {
    this.context = EnvironmentUtil.getApiUrl();
  }
}
