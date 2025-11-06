/*
 *
 */
const PROXY_CONFIG = [
    {
        context: [
            "/websocket-notifier"
        ],
        target: "wss://localhost:8443/uasdm/", 
        "changeOrigin": true,       // solves CORS Error in F12, does not seem to work with ws connections
        "logLevel": "debug",        //"info": prints out in console
        "rejectUnauthorzied": true, // must be false if not specify here
        "secure": false,            // PROD must be "true", but DEV false else "UNABLE_TO_VERIFY_LEAF_SIGNATURE"
        "strictSSL": true,          // must be false if not specify here
        "withCredentials": true,     // required for Angular to send in cookie
        cookiePathRewrite: {
            "/uasdm": "/",
        },
        ws: true
    },
    {
        context: [
            "/api", "/net/geoprism/images/", "/glyphs", "/session", "/project",
            "/logo", "/file", "/cog", "/stac", "/collection-report", "/keycloak"
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