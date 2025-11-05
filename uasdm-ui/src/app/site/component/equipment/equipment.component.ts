///
///
///

import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';

import { BsModalRef } from 'ngx-bootstrap/modal';

@Component({
    standalone: false,
  selector: 'equipment',
    templateUrl: './equipment.component.html',
    styleUrls: ['./equipment.css']
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
