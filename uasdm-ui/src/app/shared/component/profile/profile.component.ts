///
///
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
    this.bsModalRef.hide();
  }  
}
