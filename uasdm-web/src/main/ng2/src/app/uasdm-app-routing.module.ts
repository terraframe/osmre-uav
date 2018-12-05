import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ProjectsComponent } from './management/projects.component';
import { ViewerComponent } from './management/viewer.component';
import { UploadComponent } from './management/upload.component';
import { UserProfileComponent } from './management/user-profile.component';

import { AdminGuard } from './auth/admin.guard';

const routes: Routes = [
    {
        path: '',
        redirectTo: '/profile',
        pathMatch: 'full'
    },
    {
        path: 'projects',
        canActivate: [ AdminGuard ],        
        component: ProjectsComponent
    },
    {
        path: 'upload',
        component: UploadComponent
    },
    {
        path: 'viewer',
        component: ViewerComponent
    },
    {
        path: 'profile',
        component: UserProfileComponent
    },
];

@NgModule( {
    imports: [RouterModule.forRoot( routes )],
    exports: [RouterModule],
    providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }]
} )
export class UasdmAppRoutingModule { }

export const routedComponents = [ProjectsComponent, UploadComponent, UserProfileComponent, ViewerComponent];
