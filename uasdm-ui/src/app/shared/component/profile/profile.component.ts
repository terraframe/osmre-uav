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
/// License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component} from '@angular/core';

import { BsModalRef } from 'ngx-bootstrap/modal';

import { Profile } from '../../model/profile';
import { ProfileService } from '../../service/profile.service';

import { AuthService } from '../../service/auth.service';


@Component({  
  selector: 'profile',
  templateUrl: './profile.component.html',
  styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
})
export class ProfileComponent {
  public profile:Profile = {
    oid: '',
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    email: '',
    changePassword:false    
  };
  
  externalProfile: boolean = false;
  
  constructor(private authService: AuthService, private service:ProfileService, public bsModalRef: BsModalRef) {}
  
  ngOnInit(): void {
    this.externalProfile = this.authService.isExternalProfile();
  }
  
  onSubmit():void {
    if(!this.profile.changePassword) {
      delete this.profile.password;
    }
	  
    this.service.apply(this.profile).then(profile => {
      this.bsModalRef.hide();
    });
  }  
  
  cancel():void {
    if (!this.externalProfile)
    {
      this.service.unlock(this.profile.oid).then(profile => {
        this.bsModalRef.hide();
      });
    }
    else
    {
      this.bsModalRef.hide();
    }
  }  
}