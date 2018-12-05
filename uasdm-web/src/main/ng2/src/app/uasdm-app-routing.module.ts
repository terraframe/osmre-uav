import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ProjectsComponent } from './management/projects.component';
import { ViewerComponent } from './management/viewer.component';
import { UploadComponent } from './management/upload.component';
import { UserProfileComponent } from './management/user-profile.component';
import { SearchComponent } from './management/search.component';

import { AdminGuard } from './auth/admin.guard';

const routes: Routes = [
    {
        path: '',
        redirectTo: '/tasks',
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
        path: 'tasks',
        component: UserProfileComponent
    },
    {
        path: 'search',
        component: SearchComponent
    }
];

@NgModule( {
    imports: [RouterModule.forRoot( routes )],
    exports: [RouterModule],
    providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }]
} )
export class UasdmAppRoutingModule { }

<<<<<<< HEAD
export const routedComponents = [ProjectsComponent, UploadComponent, UserProfileComponent, SearchComponent];
=======
export const routedComponents = [ProjectsComponent, UploadComponent, UserProfileComponent, ViewerComponent];
>>>>>>> f29d7e428728a2ae865210143389432c7abe33bb
