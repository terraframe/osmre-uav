///
///
///

import { Injectable } from '@angular/core';
import {
    HttpEvent,
    HttpInterceptor,
    HttpHandler,
    HttpRequest,
    HttpResponseBase,
    HttpErrorResponse
} from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

import { ErrorHandler, ErrorModalComponent } from '@shared/component';
import { environment } from 'src/environments/environment';

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    constructor(private modalService: BsModalService) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

        return next.handle(request).pipe(tap((event: HttpEvent<any>) => {
            if (event instanceof HttpResponseBase) {
                const response = event as HttpResponseBase;
                if (response.status === 302) {
                    window.location.href = environment.apiUrl + environment.loginUrl;
                    return;
                }
            }
        }, (err: HttpErrorResponse) => {
            if (err instanceof HttpErrorResponse) {
                if (err.status === 401 || err.status === 403) {
                    // redirect to the login route
                    // or show a modal
                    window.location.href = environment.apiUrl + environment.loginUrl;
                }
                else {
                    this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true, class: 'modal-xl' });
                    this.bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
                }
            }
        }));
    }
}