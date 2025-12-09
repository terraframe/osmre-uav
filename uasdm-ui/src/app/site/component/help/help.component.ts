///
///
///

import { Component, OnInit, OnDestroy, Output, EventEmitter, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { v4 as uuid } from "uuid";

import { ManagementService } from '@site/service/management.service';
import { Criteria, Filter, StacItem, StacLayer } from '@site/model/layer';
import { PageResult } from '@shared/model/page';
import EnvironmentUtil from '@core/utility/environment-util';
import { environment } from 'src/environments/environment';
import { LngLatBounds } from 'maplibre-gl';

const enum VIEW_MODE {
    FORM = 0,
    RESULTS = 1
}

@Component({
    standalone: false,
    selector: 'help',
    templateUrl: './help.component.html',
    styleUrls: []
})
export class HelpComponent {

    public isBugReportCollapsed: boolean = true;
    public isFeatureCollapsed: boolean = true;
    public isQuestionCollapsed: boolean = true;

    reportEmail: string = "example@address.com";

    constructor() {
    }

    copy(text: string) {
        navigator.clipboard.writeText(text).then(() => {
            console.log("Copied:", text);
        }).catch(err => {
            console.error("Clipboard copy failed:", err);
        });
    }

}
