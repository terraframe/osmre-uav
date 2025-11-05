///
///
///

import { Pipe, PipeTransform } from '@angular/core';
import { PhoneNumberUtil, PhoneNumberFormat } from 'google-libphonenumber';


@Pipe({
	name: 'phone',
	standalone: false
})
export class PhonePipe implements PipeTransform {

	transform(value: any, args?: string): any {
		const phoneUtil = PhoneNumberUtil.getInstance();

		if (value != null && value !== '') {
			const number = phoneUtil.parseAndKeepRawInput(value, 'US');

			if (phoneUtil.isValidNumber(number)) {
				return phoneUtil.format(number, PhoneNumberFormat.INTERNATIONAL);
			}
		}

		return value;
	}

}
