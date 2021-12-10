import {
    RSocketClient,
    JsonSerializer,
    BufferEncoders,
    IdentitySerializer,
    MESSAGE_RSOCKET_ROUTING
} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {
    ReactiveSocket
} from 'rsocket-types'

export const rsocketClient = new RSocketClient<any, any>({
    serializers: {
        data: JsonSerializer,
        metadata: IdentitySerializer
    },
    setup: {
        keepAlive: 60 * 1000,
        lifetime: 180 * 1000,
        dataMimeType: "application/json",
        metadataMimeType: MESSAGE_RSOCKET_ROUTING.string,
    },
    transport: new RSocketWebSocketClient(
        {
            url: 'ws://localhost:7000',
            wsCreator: (url: string) => new WebSocket(url),
        },
        BufferEncoders,
    ),
})

export let rsocket: ReactiveSocket<any, any>

export function connectionSuccessful(socket: ReactiveSocket<any, any>) {
    console.log("connected rsocket")
    rsocket = socket
    socket.connectionStatus().subscribe(a => {
        console.log("connection status")
        console.log(a)
    })
}