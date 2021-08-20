import { Component } from '@angular/core';
import { ComponentMetadata } from '@site/model/classification';
import { WaveLengthService } from '@site/service/classification.service';

@Component({
    selector: 'wave-length',
    template: '<classifications [metadata]="metadata"></classifications>'
})
export class WaveLengthComponent {

    metadata: ComponentMetadata

    constructor(
        private service: WaveLengthService
    ) {
        this.metadata = {
            title: 'Wave Length',
            label: 'wave length',
            service: this.service
        };
    }

}
