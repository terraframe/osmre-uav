///
///
///

import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, ViewChild } from "@angular/core";
import { ControlContainer, NgForm, FormsModule, NgModel } from "@angular/forms";
import { BsModalService } from "ngx-bootstrap/modal";
import { TypeaheadMatch, TypeaheadDirective } from "ngx-bootstrap/typeahead";
import { Observable, Observer, Subscription } from "rxjs";
import { LocalizedValue, Organization } from "@shared/model/organization";
import { OrganizationService } from "@shared/service/organization.service";
import { OrganizationHierarchyModalComponent } from "./organization-hierarchy-modal.component";
import { NgClass } from "@angular/common";

@Component({
    standalone: true,
    selector: "organization-field",
    templateUrl: "./organization-field.component.html",
    styleUrls: [],
    viewProviders: [{ provide: ControlContainer, useExisting: NgForm }],
    imports: [
        FormsModule,
        TypeaheadDirective,
        NgClass,
    ],
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

    @Output() valueChange = new EventEmitter<{ code: string, label: LocalizedValue }>();

    @ViewChild("textModel") textModel: NgModel;

    private _value: { code: string, label: LocalizedValue } = null;

    @Input()
    get value(): { code: string, label: LocalizedValue } {
        return this._value;
    }

    set value(v: { code: string, label: LocalizedValue }) {
        this._value = v;
        this.text = v?.label?.localizedValue ?? "";
        this.updateValidity();
    }


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
        this.updateValidity();
    }

    onViewTree(): void {
        const bsModalRef = this.modalService.show(OrganizationHierarchyModalComponent, {
            animated: true,
            backdrop: true, class: 'modal-xl',
            ignoreBackdropClick: true
        });
        this.subscription = bsModalRef.content.init(this.disabled, this.value, (organization:Organization) => {
            this.text = organization.label.localizedValue;
            this.setValue({ code: organization.code, label: organization.label });
        });
    }

    onTextChange(): void {
        if (this.value != null) {
            const selectedLabel = this.value.label?.localizedValue ?? "";
            const currentText = this.text ?? "";

            if (currentText !== selectedLabel) {
                this.setValue(null);
            }
        }

        this.updateValidity();
    }

    private updateValidity(): void {
        if (this.textModel == null) {
            return;
        }

        if (this.required && this.value == null) {
            this.textModel.control.setErrors({ requiredOrganization: true });
        }
        else {
            const errors = this.textModel.control.errors;

            if (errors != null) {
                delete errors["requiredOrganization"];

                this.textModel.control.setErrors(Object.keys(errors).length > 0 ? errors : null);
            }
        }
    }

}
