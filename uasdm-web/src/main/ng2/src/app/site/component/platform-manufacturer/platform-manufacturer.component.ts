import { Component } from '@angular/core';
import { ComponentMetadata } from '@site/model/classification';
import { PlatformManufacturerService } from '@site/service/classification.service';

@Component({
    selector: 'platform-manufacturer',
    template: '<classifications [metadata]="metadata"></classifications>'
})
export class PlatformManufacturerComponent {

    metadata: ComponentMetadata

    constructor(
        private service: PlatformManufacturerService
    ) {
        this.metadata = {
            title: 'Platform Manufacturer',
            label: 'manufacturer',
            service: this.service
        };
    }

}
