///
///
///

import { Validator, NG_VALIDATORS, AbstractControl } from '@angular/forms';
import { Directive } from '@angular/core';

@Directive({
    standalone: true,
    selector: '[passwordValidator]',
    providers: [{
            provide: NG_VALIDATORS,
            useExisting: PasswordValidatorDirective,
            multi: true
        }]
})
export class PasswordValidatorDirective implements Validator {

	validate(control: AbstractControl): { [key: string]: any } | null {

		if (control.value != null && control.value !== '') {

			let isValid: boolean = true;

			if (!RegExp('(?=.*[0-9].*[0-9]).*').test(control.value)) {
				isValid = false;
			}

			if (!RegExp('(?=.*[a-z].*[a-z]).*').test(control.value)) {
				isValid = false;
			}

			if (!RegExp('(?=.*[A-Z].*[A-Z]).*').test(control.value)) {
				isValid = false;
			}

			if (!RegExp('(?=.*[~!@#$%^&*()_-].*[~!@#$%^&*()_-]).*').test(control.value)) {
				isValid = false;
			}

			if (isValid) {
				return null;
			}

			return { passwordStrength: true };
		}

		return null;
	}
}
