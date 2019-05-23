/// <reference types="node" />

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { enableProdMode } from '@angular/core';
import { UasdmAppModule } from './app/uasdm-app.module';

//console.log("uasdm main.ts");

if (process.env.ENV === 'production') {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(UasdmAppModule)
  .then(success => console.log('Bootstrap success'))
  .catch(error => console.log(error));

