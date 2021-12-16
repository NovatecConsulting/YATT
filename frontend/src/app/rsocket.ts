import {
    RSocketClient,
    JsonSerializer,
    BufferEncoders,
    IdentitySerializer,
    encodeCompositeMetadata, MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata
} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {
    ReactiveSocket
} from 'rsocket-types'
import {MESSAGE_RSOCKET_COMPOSITE_METADATA} from "rsocket-core";
import {store} from "./store";
import {selectToken} from "../features/auth/authSlice";

export async function connectRSocket(): Promise<void> {
    return new Promise((resolve, reject) => {
        const rsocketClient = new RSocketClient<any, any>({
            serializers: {
                data: JsonSerializer,
                metadata: IdentitySerializer
            },
            setup: {
                keepAlive: 60 * 1000,
                lifetime: 180 * 1000,
                dataMimeType: "application/json",
                metadataMimeType: MESSAGE_RSOCKET_COMPOSITE_METADATA.string,
                payload: {
                    metadata: encodeCompositeMetadata([
                        [
                            MESSAGE_RSOCKET_AUTHENTICATION,
                            encodeBearerAuthMetadata(selectToken(store.getState()) ?? "no token 123"),
                        ],
                    ])
                }
            },
            transport: new RSocketWebSocketClient(
                {
                    url: 'ws://localhost:7000',
                    wsCreator: (url: string) => new WebSocket(url),
                },
                BufferEncoders,
            ),
        });
        rsocketClient.connect().then(socket => {
                console.log("connected rsocket")
                rsocket = socket
                resolve();
            },
            error => reject(error)
        )
    });
}

export let rsocket: ReactiveSocket<any, any>
