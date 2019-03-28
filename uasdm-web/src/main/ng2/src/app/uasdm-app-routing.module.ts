import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ProjectsComponent } from './management/projects.component';
import { UploadComponent } from './management/upload.component';
import { UserProfileComponent } from './management/user-profile.component';

import { AdminGuardService } from './service/admin.guard.service';

const routes: Routes = [
    {
        path: '',
        redirectTo: '/tasks',
        pathMatch: 'full'
    },
    {
        path: 'viewer',
        component: ProjectsComponent
    },
    {
        path: 'upload',
        component: UploadComponent
    },
    {
        path: 'tasks',
        component: UserProfileComponent
    }
];

@NgModule( {
    imports: [RouterModule.forRoot( routes )],
    exports: [RouterModule],
    providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }]
} )
export class UasdmAppRoutingModule { }

export const routedComponents = [ProjectsComponent, UploadComponent, UserProfileComponent];
