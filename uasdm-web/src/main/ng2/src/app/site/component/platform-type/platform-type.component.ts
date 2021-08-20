import { Component } from '@angular/core';
import { ComponentMetadata } from '@site/model/classification';
import { PlatformTypeService } from '@site/service/classification.service';

@Component({
    selector: 'platform-type',
    template: '<classifications [metadata]="metadata"></classifications>'
})
export class PlatformTypeComponent {

    metadata: ComponentMetadata

    constructor(
        private service: PlatformTypeService
    ) {
        this.metadata = {
            title: 'Platform Type',
            label: 'type',
            service: this.service
        };
    }

}
