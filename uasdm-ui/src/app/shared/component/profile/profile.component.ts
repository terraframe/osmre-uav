///
///
///

import { Component} from '@angular/core';

import { BsModalRef } from 'ngx-bootstrap/modal';

import { Profile } from '../../model/profile';
import { ProfileService } from '../../service/profile.service';

import { AuthService } from '../../service/auth.service';
import { FormsModule } from '@angular/forms';
import { MustMatchDirective } from '../../directive/must-match.directive';
import { LocalizeComponent } from '../localize/localize.component';
import { NgIf } from '@angular/common';
import { BooleanFieldComponent } from '../boolean-field/boolean-field.component';
import { PasswordValidatorDirective } from '../../directive/password-validator.directive';
import { PasswordStrengthBarComponent } from '../password-strength-bar/password-strength-bar.component';
import { LocalizePipe } from '../../pipe/localize.pipe';


@Component({
    standalone: true,
    selector: 'profile',
    templateUrl: './profile.component.html',
    styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}'],
    imports: [FormsModule, MustMatchDirective, LocalizeComponent, NgIf, BooleanFieldComponent, PasswordValidatorDirective, PasswordStrengthBarComponent, LocalizePipe]
})
export class ProfileComponent {
  public profile:Profile = {
    oid: '',
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    email: '',
    changePassword:false,
    bureau: ''
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
