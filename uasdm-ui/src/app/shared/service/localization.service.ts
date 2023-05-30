///
///
///

import { Injectable } from "@angular/core";
import { ConfigurationService } from "@core/service/configuration.service";

@Injectable()
export class LocalizationService {

    constructor(private config : ConfigurationService) {
    }


    public localize(bundle: string, key: string): string {
        const config = this.config.getConfiguration();

        if (config.localization[bundle] != null) {
            const b = config.localization[bundle];

            if (b[key] != null) {
                return b[key];
            }
        }

        return "??" + key + "??";
    }

    public get(key: string): string {
        const config = this.config.getConfiguration();

        if (config.localization[key] != null) {
            return config.localization[key];
        }

        return "??" + key + "??";
    }

    public decode(key: string): string {
        let index = key.lastIndexOf(".");

        if (index !== -1) {
            let temp = [key.slice(0, index), key.slice(index + 1)];

            return this.localize(temp[0], temp[1]);
        } else {
            return this.get(key);
        }
    }

}

