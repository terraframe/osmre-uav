///
///
///

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
import { PlatformComponent } from './component/platform/platform.component';
import { SensorComponent } from './component/sensor/sensor.component';
import { ClassificationsComponent } from './component/classification/classifications.component';
import { Endpoint } from './service/classification.service';
import { ClassificationComponent } from './component/classification/classification.component';
import { UAVsComponent } from './component/uav/uavs.component';
import { UAVsPageComponent } from './component/uav/uavs-page.component';
import { UAVComponent } from './component/uav/uav.component';
import { ReportsComponent } from './component/report/reports.component';
import { EquipmentComponent } from './component/equipment/equipment.component';

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
        path: 'viewer/:action/:oid',
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
        path: 'report',
        canActivate: [AuthGuard],
        component: ReportsComponent
    },
    {
        path: 'sensor/:oid',
        canActivate: [AuthGuard],
        component: SensorComponent,
    },
    {
        path: 'platform/:oid',
        canActivate: [AuthGuard],
        component: PlatformComponent,
    },
    {
        path: 'platform-manufacturer/:oid',
        canActivate: [AdminGuardService],
        component: ClassificationComponent,
        data: {
            title: 'Platform Manufacturer',
            label: 'manufacturer',
            baseUrl: Endpoint.PLATFORM_MANUFACTURER
        }
    },
    {
        path: 'platform-type/:oid',
        canActivate: [AdminGuardService],
        component: ClassificationComponent,
        data: {
            title: 'Platform Type',
            label: 'type',
            baseUrl: Endpoint.PLATFORM_TYPE
        }
    },
    {
        path: 'sensor-type/:oid',
        canActivate: [AdminGuardService],
        component: ClassificationComponent,
        data: {
            title: 'Sensor Type',
            label: 'type',
            baseUrl: Endpoint.SENSOR_TYPE,
            columns: [
                {
                    name: 'isMultispectral',
                    label: 'Is Multispectral',
                    type: 'boolean'
                }
            ]
        }

    },
    {
        path: 'wave-length/:oid',
        canActivate: [AdminGuardService],
        component: ClassificationComponent,
        data: {
            title: 'Wave Length',
            label: 'wave length',
            baseUrl: Endpoint.WAVE_LENGTH
        }
    },
    {
        path: 'uavs',
        canActivate: [AdminGuardService],
        component: UAVsPageComponent,
    },
    {
        path: 'uav/:oid',
        canActivate: [AuthGuard],
        component: UAVComponent,
    },
    {
        path: 'equipment',
        canActivate: [AdminGuardService],
        component: EquipmentComponent,
        data: {}
    }

];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
    providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }]
})
export class SiteRoutingModule { }