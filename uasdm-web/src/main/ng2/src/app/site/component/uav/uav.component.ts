import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { ErrorHandler } from '@shared/component';

import { UAV } from '@site/model/uav';
import { UAVService } from '@site/service/uav.service';
import { PlatformService } from '@site/service/platform.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
    selector: 'uav',
    templateUrl: './uav.component.html',
    styleUrls: []
})
export class UAVComponent implements OnInit {
    original: UAV;
    uav: UAV;
    newInstance: boolean = false;

    message: string = null;

    bureaus: { value: string, label: string }[] = [];
    platforms: { oid: string, name: string }[] = [];
    mode: string = 'READ';

    constructor(private service: UAVService, private platformService: PlatformService,
        private route: ActivatedRoute, private router: Router
    ) { }

    ngOnInit(): void {
        const oid = this.route.snapshot.params['oid'];

        if (oid === '__NEW__') {
            this.service.newInstance().then((resp: { uav: UAV, bureaus: { value: string, label: string }[] }) => {
                this.uav = resp.uav;
                this.bureaus = resp.bureaus;
                this.newInstance = true;
                this.mode = 'WRITE';
            });
        }
        else {
            this.service.get(oid).then((resp: { uav: UAV, bureaus: { value: string, label: string }[] }) => {
                this.uav = resp.uav;
                this.bureaus = resp.bureaus;
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
