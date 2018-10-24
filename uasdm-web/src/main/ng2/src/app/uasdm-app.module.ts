import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule} from '@angular/forms';
import { HttpModule, XHRBackend, RequestOptions, Http} from '@angular/http';
import { ModalModule } from 'ngx-bootstrap/modal';
import './rxjs-extensions';


import { UasdmAppComponent } from './uasdm-app.component';
import { UasdmAppRoutingModule, routedComponents } from './uasdm-app-routing.module';

import { ManagementService } from './management/management.service';

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,    
    UasdmAppRoutingModule,
    ReactiveFormsModule,
    ModalModule.forRoot()    
  ],
  declarations: [
    UasdmAppComponent,                 
    // Routing components
    routedComponents
  ],
  providers: [
    ManagementService
  ],
  bootstrap: [UasdmAppComponent],
  entryComponents: []        
})
export class UasdmAppModule { }
