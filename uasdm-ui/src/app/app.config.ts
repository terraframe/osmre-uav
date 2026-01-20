import { APP_INITIALIZER, ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { APP_BASE_HREF, HashLocationStrategy, LocationStrategy, PlatformLocation } from '@angular/common';
import { HTTP_INTERCEPTORS, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';
import { HttpErrorInterceptor } from '@core/service/http-error.interceptor';
import { ConfigurationService } from '@core/service/configuration.service';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { ModalModule } from 'ngx-bootstrap/modal';
import { CollapseModule } from 'ngx-bootstrap/collapse';

import { TableModule } from 'primeng/table';
import { SliderModule } from 'primeng/slider';
import { DropdownModule } from 'primeng/dropdown';
import { MultiSelectModule } from 'primeng/multiselect';
import { ButtonModule } from 'primeng/button';


import { ContextMenuModule } from '@perfectmemory/ngx-contextmenu';
import { TreeModule } from '@ali-hm/angular-tree-component';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { PopoverModule } from 'ngx-bootstrap/popover';
import { AlertModule } from 'ngx-bootstrap/alert';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { AccordionModule } from 'ngx-bootstrap/accordion';

export const appConfig: ApplicationConfig = {
  providers: [
    {
      provide: APP_BASE_HREF,
      useFactory: (s: PlatformLocation) => s.getBaseHrefFromDOM(),
      deps: [PlatformLocation]
    },
    provideAnimations(),
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpErrorInterceptor,
      multi: true
    },
    // CookieService,
    // ForgotPasswordService,
    // HubService,
    // DatePipe,
    ConfigurationService,
    {
      'provide': APP_INITIALIZER,
      'useFactory': (service: ConfigurationService) => {
        // Do initing of services that is required before app loads
        // NOTE: this factory needs to return a function (that then returns a promise)
        return () => service.load(); // + any other services...
      },
      'deps': [ConfigurationService],
      'multi': true,
    },
    provideRouter(routes),
    { provide: LocationStrategy, useClass: HashLocationStrategy },

    // legacy providers
    importProvidersFrom([
      // ngx-bootstrap
      ModalModule.forRoot(),
      CollapseModule.forRoot(),
      TabsModule.forRoot(),
      PopoverModule.forRoot(),
      AlertModule.forRoot(),
      BsDropdownModule.forRoot(),
      TypeaheadModule.forRoot(),
      AccordionModule.forRoot(),
      CollapseModule.forRoot(),

      // …
    ]),
    // ContextMenuModule,
    // TreeModule,
    // TableModule,
    // SliderModule,
    // DropdownModule,
    // MultiSelectModule,
    // ButtonModule,
  ]
};