import { ApplicationConfig, importProvidersFrom, inject, provideAppInitializer } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { APP_BASE_HREF, HashLocationStrategy, LocationStrategy, PlatformLocation } from '@angular/common';
import { HTTP_INTERCEPTORS, withInterceptorsFromDi } from '@angular/common/http';
import { HttpErrorInterceptor } from '@core/service/http-error.interceptor';
import { ConfigurationService } from '@core/service/configuration.service';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ModalModule } from 'ngx-bootstrap/modal';
import { CollapseModule } from 'ngx-bootstrap/collapse';

import { TabsModule } from 'ngx-bootstrap/tabs';
import { PopoverModule } from 'ngx-bootstrap/popover';
import { AlertModule } from 'ngx-bootstrap/alert';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { AccordionModule } from 'ngx-bootstrap/accordion';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';

export const appConfig: ApplicationConfig = {
  providers: [
    {
      provide: APP_BASE_HREF,
      useFactory: (s: PlatformLocation) => s.getBaseHrefFromDOM(),
      deps: [PlatformLocation]
    },
    provideAnimationsAsync(),
    provideAnimations(),
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpErrorInterceptor,
      multi: true
    },
    ConfigurationService,
    provideAppInitializer(() => {
      const service = inject(ConfigurationService);
      return service.load();
    }),
    provideRouter(routes),
    { provide: LocationStrategy, useClass: HashLocationStrategy },
    providePrimeNG({
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: false || 'none'
        }
      }
    }),
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