import { Injectable, OnDestroy } from "@angular/core";
import { WebSockets } from "@core/utility/web-sockets";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

@Injectable({ providedIn: 'root' })
export class WebsocketService implements OnDestroy {
    notifier: WebSocketSubject<any>;

    constructor() {
        this.connect();
    }
    private connect() {
        try {

            this.notifier = webSocket(WebSockets.buildBaseUrl() + "/websocket-notifier/notify");
        }
        catch (e) {
            console.log('Unable to connect websocket', e);
        }
    }

    getNotifier(): WebSocketSubject<any> {
        return this.notifier;
    }

    ngOnDestroy(): void {
        this.notifier.complete();

        this.notifier = null;
    }


}
