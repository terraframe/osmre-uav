///
///
///

import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from "@angular/core";
import { BsModalService } from "ngx-bootstrap/modal";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";
import { Observable, Observer, Subscription } from "rxjs";
import { LocalizedValue, Organization } from "@shared/model/organization";
import { OrganizationService } from "@shared/service/organization.service";
import { OrganizationHierarchyModalComponent } from "./organization-hierarchy-modal.component";

@Component({
    selector: "organization-field",
    templateUrl: "./organization-field.component.html",
    styleUrls: []
})
export class OrganizationFieldComponent implements OnInit, OnDestroy {

    @Input() organizationType: string;
    @Input() rootCode: string;

    @Input() name: string;
    @Input() disabled: boolean = false;
    @Input() required: boolean = false;
    @Input() customStyles: string = "";
    @Input() classNames: string = "";
    @Input() container: string = null;

    @Input() value: { code: string, label: LocalizedValue } = null;

    @Output() valueChange = new EventEmitter<{ code: string, label: LocalizedValue }>();

    loading: boolean = false;
    text: string = "";

    typeahead: Observable<any> = null;
    subscription: Subscription = null;

    constructor(
        private modalService: BsModalService,
        private service: OrganizationService) { }

    ngOnInit(): void {
        this.typeahead = new Observable((observer: Observer<any>) => {
            this.service.search(this.text).then(results => {
                observer.next(results);
            });
        });

        if (this.value != null) {
            this.text = this.value.label.localizedValue;
        }
    }

    ngOnDestroy(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }
    }

    typeaheadOnSelect(match: TypeaheadMatch): void {
        if (match != null) {
            const item: Organization = match.item;
            this.text = item.label.localizedValue;

            if (this.value == null || this.value.code !== item.code) {
                this.setValue({ code: item.code, label: item.label });
            }
        } else if (this.value != null) {
            this.setValue(null);
        }
    }

    setValue(value: { code: string, label: LocalizedValue }): void {
        this.value = value;
        this.valueChange.emit(this.value);
    }

    onViewTree(): void {
        const bsModalRef = this.modalService.show(OrganizationHierarchyModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.subscription = bsModalRef.content.init(this.disabled, this.value, (organization:Organization) => {
            this.text = organization.label.localizedValue;
            this.setValue({ code: organization.code, label: organization.label });
        });
    }

    onTextChange(): void {
        if (this.value != null && (this.text == null || this.text.length === 0)) {
            this.setValue(null);
        }
    }

}
