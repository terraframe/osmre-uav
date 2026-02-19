///
///
///

import { inject, Injectable, OnDestroy } from "@angular/core";
import { WebSockets } from "@core/utility/web-sockets";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Observable } from "rxjs";
import { User } from "@shared/model/user";
import { Store } from "@ngrx/store";
import { getUser } from "src/app/state/session.state";

@Injectable({ providedIn: 'root' })
export class WebsocketService implements OnDestroy {

    private store = inject(Store);
    user$: Observable<User | null> = this.store.select(getUser);

    notifier: WebSocketSubject<any>;


    constructor() {

        this.connect();

        // If the user changes then make sure the websockets are destroyed and reconnected
        this.user$.pipe(takeUntilDestroyed()).subscribe((user) => {
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
