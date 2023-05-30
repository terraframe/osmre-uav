///
///
///

import { Injectable } from '@angular/core';

import { LocalizationService } from './localization.service';

@Injectable()
export class DateService {
  overlapMessage = { 
    "type": "ERROR",  
    "message":this.localizationService.decode("manage.versions.overlap.message")
  }
  
  gapMessage = {
    "type": "WARNING",  
    "message":this.localizationService.decode("manage.versions.gap.message")
  }

  constructor(private localizationService: LocalizationService) {}
  
  public formatDateForDisplay(date: string | Date): string {
    if(!date){
      return "";
    }
    
    if(date instanceof Date){
      return this.getDateString(date);
    }
    else{
      return date.split('T')[0];
    }
  }
  
  // @param value as yyyy-mm-dd
  getDateFromDateString(value: string){
    return new Date(+value.split("-")[0], +value.split("-")[1]-1, +value.split("-")[2]);
  }
  
  getDateString(date:Date): string {
    if(date instanceof Date){
      let year = date.getFullYear();
      let month:number|string = date.getMonth()+1;
      let dt:number|string = date.getDate();
      
      if (dt < 10) {
        dt = '0' + dt;
      }
      if (month < 10) {
        month = '0' + month;
      }
      
      return year + "-" + month + "-" + dt;
    }
    
    return null;
  }

}