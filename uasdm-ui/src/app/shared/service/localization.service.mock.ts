///
///
///

import { Injectable } from '@angular/core';


@Injectable()
export class MockLocalizationService {

  public parseNumber(value: string) : number {
    return parseFloat(value);
  }    
    
  public formatNumber(value:any): string {
    return value + '';
  }
    
  public localize(bundle: string, key: string): string {
    return bundle + '.' + key;
  }
    
  public get(key: string): string {
    return key;
  }
  
  public decode(key: string): string {
    return key;	  
  }
}
