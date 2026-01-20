///
///
///

import { Component, OnInit } from '@angular/core';
import { UasdmHeaderComponent } from '../../../shared/component/header/header.component';
import { UAVsComponent } from './uavs.component';

@Component({
    standalone: true,
    selector: 'uavs-page',
    templateUrl: './uavs-page.component.html',
    imports: [UasdmHeaderComponent, UAVsComponent]
})
export class UAVsPageComponent implements OnInit {

    constructor() { }

    ngOnInit(): void {
        
    }

}
