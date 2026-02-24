///
///
///

import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';

import { BsModalRef } from 'ngx-bootstrap/modal';
import { UasdmHeaderComponent } from '@shared/component/header/header.component';
import { NgClass } from '@angular/common';
import { CollapseDirective } from 'ngx-bootstrap/collapse';
import { ClassificationsComponent } from '../classification/classifications.component';
import { SensorsComponent } from '../sensor/sensors.component';
import { PlatformsComponent } from '../platform/platforms.component';

@Component({
    standalone: true,
    selector: 'equipment',
    templateUrl: './equipment.component.html',
    styleUrls: ['./equipment.css'],
    imports: [UasdmHeaderComponent, NgClass, CollapseDirective, ClassificationsComponent, SensorsComponent, PlatformsComponent]
})
export class EquipmentComponent implements OnInit {

    bsModalRef: BsModalRef;
    
    isSensorTypeCollapsed: boolean = true;
    isSensorWavelengthCollapsed: boolean = true;
    isPlatformTypeCollapsed: boolean = true;
    isPlatformManufacturerCollapsed: boolean = true;
    
    constructor() { }

    ngOnInit(): void {
    }
    

}
