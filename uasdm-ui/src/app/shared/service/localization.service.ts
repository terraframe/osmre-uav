///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <ehttp://www.gnu.org/licenses/>.
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

