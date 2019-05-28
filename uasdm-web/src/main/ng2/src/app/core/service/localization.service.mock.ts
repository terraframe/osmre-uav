///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <ehttp://www.gnu.org/licenses/>.
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
