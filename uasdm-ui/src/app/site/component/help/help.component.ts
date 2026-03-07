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
import { UasdmHeaderComponent } from '@shared/component/header/header.component';
import { LocalizedValue } from '@shared/model/organization';
import { HelpService, HelpPageContentResponse } from '@site/service/help-service';
import { MarkdownComponent } from 'ngx-markdown';
import { OrganizationFieldComponent } from '@shared/component/organization-field/organization-field.component';
import { AuthService } from '@shared/service/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

const enum VIEW_MODE {
    FORM = 0,
    RESULTS = 1
}

@Component({
    standalone: true,
    selector: 'help',
    templateUrl: './help.component.html',
    styleUrls: ['./help.component.scss'],
    imports: [CommonModule, FormsModule, UasdmHeaderComponent, MarkdownComponent, OrganizationFieldComponent]
})
export class HelpComponent implements OnInit {

    public isBugReportCollapsed: boolean = true;
    public isFeatureCollapsed: boolean = true;
    public isQuestionCollapsed: boolean = true;

    reportEmail: string = "example@address.com";

    public data: HelpPageContentResponse = { markdown: "", organization: null };
    public renderedMarkdown = '';
    public showRenderedMarkdown: boolean = true;

    public isAdmin: boolean = false;

    public isEditing: boolean = false;
    public editMarkdown: string = '';
    public isSaving: boolean = false;

    constructor(private helpService: HelpService, private authService: AuthService) { }

    ngOnInit(): void {
        this.isAdmin = this.authService.isAdmin();
        this.fetchData();
    }

    onOrganizationChange(): void {

        this.fetchData();
    }

    fetchData() {
        let orgCode = this.data?.organization?.code;

        this.helpService.content(orgCode).subscribe(response => {
            this.data = response;
            this.renderedMarkdown = this.data.markdown;

            this.showRenderedMarkdown = false;
            setTimeout(() => {
                this.showRenderedMarkdown = true;
            },0);
        });
    }

    startEdit(): void {
        this.editMarkdown = this.data?.markdown ?? '';
        this.isEditing = true;
    }

    cancelEdit(): void {
        this.editMarkdown = this.data?.markdown ?? '';
        this.isEditing = false;
    }

    saveEdit(): void {
        if (!this.data?.organization?.code) return;

        this.isSaving = true;

        this.helpService.edit(this.data.organization.code, this.editMarkdown).subscribe({
            next: () => {
                if (this.data) {
                    this.data = {
                        ...this.data,
                        markdown: this.editMarkdown
                    };
                }
                this.renderedMarkdown = this.editMarkdown;
                this.isEditing = false;
                this.isSaving = false;
            },
            error: (err) => {
                console.error('Failed to save help markdown', err);
                this.isSaving = false;
            }
        });
    }

    copy(text: string) {
        navigator.clipboard.writeText(text).then(() => {
            console.log("Copied:", text);
        }).catch(err => {
            console.error("Clipboard copy failed:", err);
        });
    }

}
