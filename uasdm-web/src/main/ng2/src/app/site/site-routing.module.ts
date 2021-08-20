import { NgModule } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule } from '@angular/router';

import { ProjectsComponent } from './component/projects.component';
import { UploadComponent } from './component/upload.component';
import { TasksComponent } from './component/tasks.component';
import { SensorsComponent } from './component/sensor/sensors.component';
import { PlatformsComponent } from './component/platform/platforms.component';

import { CanDeactivateGuardService } from "./service/can.deactivate.guard.service";
import { AuthGuard, AdminGuardService } from '../shared/service/guard.service';
import { PlatformManufacturerComponent } from './component/platform-manufacturer/platform-manufacturer.component';
import { PlatformTypeComponent } from './component/platform-type/platform-type.component';
import { SensorTypeComponent } from './component/sensor-type/sensor-type.component';
import { WaveLengthComponent } from './component/wave-length/wave-length.component';

const routes: Routes = [
    {
        path: '',
        canActivate: [AuthGuard],
        component: ProjectsComponent
    },
    {
        path: 'viewer',
        canActivate: [AuthGuard],
        component: ProjectsComponent
    },
    {
        path: 'upload',
        component: UploadComponent,
        canDeactivate: [CanDeactivateGuardService]
    },
    {
        path: 'tasks',
        canActivate: [AuthGuard],
        component: TasksComponent
    },
    {
        path: 'sensors',
        canActivate: [AdminGuardService],
        component: SensorsComponent,
    },
    {
        path: 'platforms',
        canActivate: [AdminGuardService],
        component: PlatformsComponent,
    },
    {
        path: 'platform-manufacturers',
        canActivate: [AdminGuardService],
        component: PlatformManufacturerComponent,
    },
    {
        path: 'platform-types',
        canActivate: [AdminGuardService],
        component: PlatformTypeComponent,
    },
    {
        path: 'sensor-types',
        canActivate: [AdminGuardService],
        component: SensorTypeComponent,
    },
    {
        path: 'wave-lengths',
        canActivate: [AdminGuardService],
        component: WaveLengthComponent,
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
    providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }]
})
export class SiteRoutingModule { }