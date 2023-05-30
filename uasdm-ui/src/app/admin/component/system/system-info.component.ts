///
///
///

import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { AuthService } from '@shared/service/auth.service';


@Component({
    selector: 'system-info',
    templateUrl: './system-info.component.html',
    styleUrls: ['./system-info.css']
})
export class SystemInfoComponent implements OnInit {
    
    userName: string = "";
    admin: boolean = false;

    private bsModalRef: BsModalRef;
    
    constructor(private modalService: BsModalService, private authService: AuthService) { }

    ngOnInit(): void {
        this.userName = this.authService.getUserName();
        this.admin = this.authService.isAdmin();
    }

}
