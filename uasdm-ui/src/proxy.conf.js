const PROXY_CONFIG = [
    {
        context: [
            "/websocket-notifier"
        ],
        target: "wss://localhost:8443/uasdm/",
        "changeOrigin": true,       // solves CORS Error in F12
        "logLevel": "debug",         //"info": prints out in console
        "rejectUnauthorzied": false, // must be false if not specify here
        "secure": false,            // PROD must be "true", but DEV false else "UNABLE_TO_VERIFY_LEAF_SIGNATURE"
        "strictSSL": true,          // must be false if not specify here
        ws: true
    },
    {
        context: [
            "/net/geoprism/images/", "/glyphs", "/session", "/project",
            "/logo", "/uav", "/product", "/platform-manufacturer", "/platform",
            "/platform-type", "/sensor", "/wave-length", "/sensor-type",
            "/file", "/cog", "/stac"
        ],
        target: "https://localhost:8443/uasdm/",
        "changeOrigin": true,       // solves CORS Error in F12
        "logLevel": "debug",         //"info": prints out in console
        "rejectUnauthorzied": true, // must be false if not specify here
        "secure": false,            // PROD must be "true", but DEV false else "UNABLE_TO_VERIFY_LEAF_SIGNATURE"
        "strictSSL": true,          // must be false if not specify here
        "withCredentials": true,     // required for Angular to send in cookie
        cookiePathRewrite: {
            "/uasdm": "/",
        }
    },
    {
        context: [
            "/uasdm"
        ],
        target: "https://localhost:8443/",
        "changeOrigin": true,       // solves CORS Error in F12
        "logLevel": "debug",         //"info": prints out in console
        "rejectUnauthorzied": true, // must be false if not specify here
        "secure": false,            // PROD must be "true", but DEV false else "UNABLE_TO_VERIFY_LEAF_SIGNATURE"
        "strictSSL": true,          // must be false if not specify here
        "withCredentials": true,     // required for Angular to send in cookie
        cookiePathRewrite: {
            "/uasdm": "/",
        }
    }
]
module.exports = PROXY_CONFIG;