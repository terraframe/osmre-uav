import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Configuration } from "@core/model/application";
import { firstValueFrom } from "rxjs";
import { environment } from "src/environments/environment";

@Injectable()
export class ConfigurationService {

    configuration: Configuration;

    constructor(private http: HttpClient) {
    }

    load(): Promise<Configuration> {
        return firstValueFrom(this.http.get<Configuration>(environment.apiUrl + "/project/configuration")).then(configuration => {
            this.configuration = configuration;

            return this.configuration;
        });
    }

    logout(): Promise<void> {
        return firstValueFrom(this.http.post<void>(environment.apiUrl + "/session/logout", {}));
    }

    getConfiguration(): Configuration {
        return this.configuration;
    }

    isKeycloakEnabled(): boolean {
        return this.getConfiguration().uasdmKeycloakEnabled;
    }

    getAppDisclaimer(): string {
        return this.getConfiguration().uasAppDisclaimer;
    }

    getContextPath(): string {
        return this.getConfiguration().contextPath;
    }
}
