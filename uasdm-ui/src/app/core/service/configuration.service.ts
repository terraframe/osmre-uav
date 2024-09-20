///
///
///

import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Configuration } from "@core/model/application";
import { firstValueFrom } from "rxjs";
import { environment } from "src/environments/environment";
import EnvironmentUtil from '@core/utility/environment-util';

@Injectable()
export class ConfigurationService {

    configuration: Configuration;

    context: string;

    constructor(private http: HttpClient) {
        this.context = EnvironmentUtil.getApiUrl();
    }

    load(): Promise<Configuration> {
        return firstValueFrom(this.http.get<Configuration>(environment.apiUrl + "/project/configuration")).then(configuration => {
            this.configuration = configuration;

            return this.configuration;
        });
    }

    logout(): Promise<void> {
        return firstValueFrom(this.http.get<void>(environment.apiUrl + "/api/session/logout", {}));
    }

    getConfiguration(): Configuration {
        return this.configuration;
    }

    isKeycloakEnabled(): boolean {
        return this.getConfiguration().uasdmKeycloakEnabled;
    }

    isRequireKeycloakLogin(): boolean {
        return this.getConfiguration().uasdmRequireKeycloakLogin;
    }

    getAppDisclaimer(): string {
        return this.getConfiguration().uasAppDisclaimer;
    }

    getContextPath(): string {
        return this.getConfiguration().contextPath;
    }

    getKnowStacURL(): string {
        return this.getConfiguration().knowStacUrl;
    }

}
