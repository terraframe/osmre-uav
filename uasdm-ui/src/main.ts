/// <reference types="node" />

import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { UasdmAppModule } from './app/uasdm-app.module';
import { environment } from './environments/environment';


if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(UasdmAppModule)
  .then(success => console.log('Bootstrap success'))
  .catch(error => console.log(error));

