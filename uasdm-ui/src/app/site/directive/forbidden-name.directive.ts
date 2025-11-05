///
///
///

import { Directive, Input } from "@angular/core";
import { AbstractControl, NG_VALIDATORS, Validator, ValidatorFn } from "@angular/forms";

export function forbiddenNameValidator( nameRe: RegExp ): ValidatorFn {
    return ( control: AbstractControl ): { [key: string]: any } | null => {
        const forbidden = nameRe.test( control.value );
        return forbidden ? { 'forbiddenName': { value: control.value } } : null;
    };
}

@Directive( {
    standalone: false,
  selector: '[forbiddenName]',
    providers: [{ provide: NG_VALIDATORS, useExisting: ForbiddenNameDirective, multi: true }]
} )
export class ForbiddenNameDirective implements Validator {
    validate( control: AbstractControl ): { [key: string]: any } | null {
        return forbiddenNameValidator( new RegExp( /[\W<>\-+=!@#$%^&*?/\\']/gm  ) )( control );
    }
  }