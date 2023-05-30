///
///
///

import { Injectable } from "@angular/core";
import { CanDeactivate } from "@angular/router";

import { UploadComponent } from "../component/upload.component";

@Injectable()
export class CanDeactivateGuardService implements CanDeactivate<UploadComponent> {
  canDeactivate(component: UploadComponent): boolean {
   
    if(component.canDeactivate()){
        if (confirm("An upload is currently in progress. Are you sure you want to leave?")) {
            return true;
        } else {
            return false;
        }
    }
    return true;
  }
}