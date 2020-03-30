import { Validator, NG_VALIDATORS, AbstractControl } from '@angular/forms';
import { Directive } from '@angular/core';
import { PhoneNumberUtil } from 'google-libphonenumber';

@Directive({
	selector: '[phoneNumber]',
	providers: [{
		provide: NG_VALIDATORS,
		useExisting: PhoneNumberValidatorDirective,
		multi: true
	}]
})
export class PhoneNumberValidatorDirective implements Validator {

	validate(control: AbstractControl): { [key: string]: any } | null {
		if (control.value !== '') {
			try {

				const phoneUtil = PhoneNumberUtil.getInstance();
				const phoneNumber = '' + control.value + '';
				const pNumber = phoneUtil.parseAndKeepRawInput(phoneNumber, 'US');
				const isValidNumber = phoneUtil.isValidNumber(pNumber);

				if (isValidNumber) {
					return null;
				}
			} catch (e) {
				console.log(e);
				return {
					phoneNumber: true
				};
			}

			return {
				phoneNumber: true
			};
		}

		return null;
	}
}
