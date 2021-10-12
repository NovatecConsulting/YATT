import {baseUrl} from "../features/api/apiSlice";
import ndjsonStream, {CancelCallback, Result} from "can-ndjson-stream";

export async function subscribe<UpdateType>(
    path: string,
    onUpdate: (update: UpdateType) => void,
    token?: string
): Promise<CancelCallback> {
    // TODO try resubscribe if lost connection to server
    const response = await fetch(`${baseUrl}${path}`, {
        headers: new Headers({
            'Accept': 'application/x-ndjson',
            ...(token && {'Authorization': `Bearer ${token}`})
        })
    });

    if (response.body) {
        const stream = ndjsonStream<UpdateType>(response.body);
        const reader = stream.getReader();

        let processResult: (result: Result<UpdateType>) => void;
        reader.read().then(processResult = (result) => {
            if (result.done) {
                return;
            }

            if (result.value) {
                onUpdate(result.value);
            }
            reader.read().then(processResult);
        });

        return (reason?: string) => {
            return reader.cancel(reason);
        };
    } else {
        throw 'no response body';
    }
}