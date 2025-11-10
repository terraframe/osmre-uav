///
///
///

import { Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

import { ErrorHandler, ErrorModalComponent } from '@shared/component';

import { SessionService } from '@shared/service/session.service';
import { ConfigurationService } from '@core/service/configuration.service';
import EnvironmentUtil from '@core/utility/environment-util';

@Component({
  standalone: false,
  selector: 'login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  context: string;
  username: string = '';
  password: string = '';

  private bsModalRef: BsModalRef;

  sub: Subscription;

  keycloakEnabled: boolean;
  
  requireKeycloakLogin: boolean;

  constructor(private configuration: ConfigurationService, private service: SessionService, private router: Router, private route: ActivatedRoute, private modalService: BsModalService) {
    this.context = EnvironmentUtil.getApiUrl();
    this.keycloakEnabled = configuration.isKeycloakEnabled();
    this.requireKeycloakLogin = configuration.isRequireKeycloakLogin();
  }

  ngOnInit(): void {
    this.sub = this.route.params.subscribe(params => {

      if (params['errorMsg'] != null) {
        this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true, class: 'modal-xl' });

        let encodedError = params['errorMsg'];
        let decodedError = encodedError.replaceAll("+", " ");

        this.bsModalRef.content.message = decodedError;
      }
    });
  }

  onClickKeycloak(): void {
    window.location.href = this.context + "/keycloak/loginRedirect";
  }

  onSubmit(): void {
    this.service.login(this.username, this.password).then(response => {
      this.router.navigate(["/menu"]);
    });
  }

  public error(err: HttpErrorResponse): void {
    this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
  }
}
