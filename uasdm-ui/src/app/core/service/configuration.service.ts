///
///
///

import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Configuration } from "@core/model/application";
import { firstValueFrom } from "rxjs";
import { environment } from "src/environments/environment";
import EnvironmentUtil from '@core/utility/environment-util';
const mapboxKey = 'pk.eyJ1IjoidGVycmFmcmFtZSIsImEiOiJjanZxNTFnaTYyZ2RuNDlxcmNnejNtNjN6In0.-kmlS8Tgb2fNc1NPb5rJEQ';


@Injectable()
export class ConfigurationService {

    configuration: Configuration;

    context: string;

    constructor(private http: HttpClient) {
        this.context = EnvironmentUtil.getApiUrl();
    }

    load(): Promise<Configuration> {
        return firstValueFrom(this.http.get<Configuration>(environment.apiUrl + "/api/project/configuration")).then(configuration => {
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

    getMapboxKey(): string {
        return mapboxKey;
    }

}
