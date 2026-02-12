///
///
///

import { Injectable, OnDestroy } from "@angular/core";
import { WebSockets } from "@core/utility/web-sockets";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import { SessionService } from "./session.service";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";

@Injectable({ providedIn: 'root' })
export class WebsocketService implements OnDestroy {
    notifier: WebSocketSubject<any>;

    constructor(private sessionService: SessionService) {

        this.connect();

        this.sessionService.getUser().pipe(takeUntilDestroyed()).subscribe((user) => {
            if (this.notifier != null) {
                this.notifier.complete();
            }

            if (user != null) {
                this.notifier = webSocket(WebSockets.buildBaseUrl() + "/websocket-notifier/notify");
            }
        });

    }

    connect(): void {
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
