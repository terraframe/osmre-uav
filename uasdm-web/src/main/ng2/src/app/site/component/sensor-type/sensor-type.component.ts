import { Component } from '@angular/core';
import { ComponentMetadata } from '@site/model/classification';
import { SensorTypeService } from '@site/service/classification.service';

@Component({
    selector: 'sensor-type',
    template: '<classifications [metadata]="metadata"></classifications>'    
})
export class SensorTypeComponent {

    metadata: ComponentMetadata

    constructor(
        private service: SensorTypeService
    ) {
        this.metadata = {
            title: 'Sensor Type',
            label: 'type',
            service: this.service
        };
    }

}
