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
import { UAVComponent } from './component/uav/uav.component';
import { ReportsComponent } from './component/report/reports.component';

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
        path: 'sensors',
        canActivate: [AdminGuardService],
        component: SensorsComponent,
    },
    {
        path: 'sensor/:oid',
        canActivate: [AdminGuardService],
        component: SensorComponent,
    },
    {
        path: 'platforms',
        canActivate: [AdminGuardService],
        component: PlatformsComponent,
    },
    {
        path: 'platform/:oid',
        canActivate: [AdminGuardService],
        component: PlatformComponent,
    },
    {
        path: 'platform-manufacturers',
        canActivate: [AdminGuardService],
        component: ClassificationsComponent,
        data: {
            title: 'Platform Manufacturer',
            label: 'manufacturer',
            baseUrl: Endpoint.PLATFORM_MANUFACTURER
        }
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
        path: 'platform-types',
        canActivate: [AdminGuardService],
        component: ClassificationsComponent,
        data: {
            title: 'Platform Type',
            label: 'type',
            baseUrl: Endpoint.PLATFORM_TYPE
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
        path: 'sensor-types',
        canActivate: [AdminGuardService],
        component: ClassificationsComponent,
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
        path: 'wave-lengths',
        canActivate: [AdminGuardService],
        component: ClassificationsComponent,
        data: {
            title: 'Wave Length',
            label: 'wave length',
            baseUrl: Endpoint.WAVE_LENGTH
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
        component: UAVsComponent,
    },
    {
        path: 'uav/:oid',
        canActivate: [AdminGuardService],
        component: UAVComponent,
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
    providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }]
})
export class SiteRoutingModule { }