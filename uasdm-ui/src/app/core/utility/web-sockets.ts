///
///
///


import EnvironmentUtil from "@core/utility/environment-util";

export class WebSockets {

    static buildBaseUrl(): string {
        let protocol = "wss";

        if (window.location.protocol.indexOf("https") !== -1) {
            protocol = "wss"; // Web Socket Secure
        } else {
            protocol = "ws";
        }

        let baseUrl = protocol + "://" + window.location.hostname + (window.location.port ? ":" + window.location.port : "");

        // console.log('pathname', window.location.pathname);

        let path = window.location.pathname.split("#")[0];
        path = path.split("/")[1];

        // console.log('path', path);

        if (path != null) {
            baseUrl += "/" + path;
        }

        baseUrl += EnvironmentUtil.getApiUrl();

        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length - 1);
        }

        return baseUrl;
    }

}
