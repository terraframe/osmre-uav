///
///
///

import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { ErrorHandler } from '@shared/component';

import { UAV } from '@site/model/uav';
import { UAVService } from '@site/service/uav.service';
import { PlatformService } from '@site/service/platform.service';
import { AuthService } from '@shared/service/auth.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UasdmHeaderComponent } from '../../../shared/component/header/header.component';
import { NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrganizationFieldComponent } from '../../../shared/component/organization-field/organization-field.component';
import { FilterPipe } from '../../../shared/pipe/filter.pipe';

@Component({
    standalone: true,
    selector: 'uav',
    templateUrl: './uav.component.html',
    styleUrls: [],
    imports: [UasdmHeaderComponent, NgIf, FormsModule, NgFor, RouterLink, OrganizationFieldComponent, FilterPipe]
})
export class UAVComponent implements OnInit {
    
    isAdmin:boolean = false;
    
    original: UAV;
    uav: UAV;
    newInstance: boolean = false;

    message: string = null;

    platforms: { oid: string, name: string }[] = [];
    mode: string = 'READ';

    constructor(private service: UAVService, private platformService: PlatformService, private authService: AuthService,
        private route: ActivatedRoute, private router: Router
    ) {
        this.isAdmin = this.authService.isAdmin();
     }

    ngOnInit(): void {
        const oid = this.route.snapshot.params['oid'];

        if (oid === '__NEW__') {
            this.service.newInstance().then((resp: { uav: UAV, bureaus: { value: string, label: string }[] }) => {
                this.uav = resp.uav;
                this.newInstance = true;
                this.mode = 'WRITE';
            });
        }
        else {
            this.service.get(oid).then((resp: { uav: UAV, bureaus: { value: string, label: string }[] }) => {
                this.uav = resp.uav;
                this.original = JSON.parse(JSON.stringify(this.uav));
            });
        }

        this.platformService.getAll().then(platforms => {
            this.platforms = platforms;
        });
    }

    handleOnSubmit(): void {
        this.message = null;

        this.service.apply(this.uav).then(data => {
            this.uav = data;
            this.mode = 'READ';

            if (this.newInstance) {
                this.router.navigate(['/site/uav', data.oid]);
                this.newInstance = false;
                this.original = data;
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleOnCancel(): void {
        this.message = null;

        this.uav = JSON.parse(JSON.stringify(this.original));
        this.mode = 'READ';
    }

    handleOnEdit(): void {
        this.mode = 'WRITE';
    }



    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
