import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
	selector: 'boolean-field',
	templateUrl: './boolean-field.component.html',
	styles: ['.modal-form .holder .check-block .chk-area {margin: 10px 0px 0 0;}']
})
export class BooleanFieldComponent {

	@Input() value: boolean = false;
	@Input() disabled: boolean = false;
	@Input() localizeLabelKey: string = ""; // localization key used to localize in the component template
	@Input() label: string = ""; // raw string input

	@Output() public valueChange = new EventEmitter<boolean>();

	constructor() { }

	toggle(): void {
		if (!this.disabled) {
			this.value = !this.value;

			this.valueChange.emit(this.value);
		}
	}
}