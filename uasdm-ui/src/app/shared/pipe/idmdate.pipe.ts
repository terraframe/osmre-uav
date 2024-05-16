///
///
///

import {Inject, LOCALE_ID, Pipe, PipeTransform} from '@angular/core';
import {DatePipe} from '@angular/common';

// https://github.com/angular/angular/blob/5.0.4/packages/common/src/pipes/date_pipe.ts#L137
@Pipe({name: 'idmdate'})
export class IdmDatePipe implements PipeTransform {
    constructor(@Inject(LOCALE_ID) private locale: string) {
    }

    transform(date: Date | string, format: string = "MMM dd, yyyy hh:mm:ss a", useLocaleTimeZone: boolean = true): string {
        date = new Date(date);

        let transformed: string = null;
        if (this.isValidDate(date)) {
            transformed = new DatePipe(this.locale, useLocaleTimeZone ? null : 'UTC').transform(date, format);

            if (useLocaleTimeZone) {
                transformed = transformed + " " + this.getTimezoneName();
            }
        }

        return transformed;
    }

    // https://stackoverflow.com/questions/9772955/how-can-i-get-the-timezone-name-in-javascript
    private getTimezoneName() {
        const today = new Date();
        const short = today.toLocaleDateString(undefined);
        const full = today.toLocaleDateString(undefined, {timeZoneName: 'short'});

        // Trying to remove date from the string in a locale-agnostic way
        const shortIndex = full.indexOf(short);
        if (shortIndex >= 0) {
            const trimmed = full.substring(0, shortIndex) + full.substring(shortIndex + short.length);

            // by this time `trimmed` should be the timezone's name with some punctuation -
            // trim it from both sides
            return trimmed.replace(/^[\s,.\-:;]+|[\s,.\-:;]+$/g, '');

        } else {
            // in some magic case when short representation of date is not present in the long one
            return Intl.DateTimeFormat().resolvedOptions().timeZone;
        }
    }

    private isValidDate(d: Date) {
        return d instanceof Date && !isNaN(d.getTime());
    }
}