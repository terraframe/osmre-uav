import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ProjectsComponent } from './component/projects.component';
import { UploadComponent } from './component/upload.component';
import { UserProfileComponent } from './component/user-profile.component';

import { CanDeactivateGuardService } from "./service/can.deactivate.guard.service";
import { AuthGuard } from '../shared/service/guard.service';

const routes: Routes = [
    {
        path: '',
        canActivate: [ AuthGuard ],
        component: UserProfileComponent
    },
    {
        path: 'viewer',
        canActivate: [ AuthGuard ],
        component: ProjectsComponent
    },
    {
        path: 'upload',
        component: UploadComponent,
        canDeactivate: [CanDeactivateGuardService]
    },
    {
        path: 'tasks',
        canActivate: [ AuthGuard ],
        component: UserProfileComponent
    },
];

@NgModule( {
    imports: [RouterModule.forChild( routes )],
    exports: [RouterModule],
    providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }]
} )
export class SiteRoutingModule { }