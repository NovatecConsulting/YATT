import {baseUrl} from "../features/api/apiSlice";
import ndjsonStream from "can-ndjson-stream";

export async function subscribe<UpdateType>(
    path: string,
    onUpdate: (update: UpdateType) => void,
    token?: string
): Promise<() => void> {
    const response = await fetch(`${baseUrl}/projects`, {
        headers: new Headers({
            'Accept': 'application/x-ndjson',
            ...(token && {'Authorization': `Bearer ${token}`})
        })
    });
    if (response.body) {
        const stream = ndjsonStream<UpdateType>(response.body);
        const reader = stream.getReader();
        let read: (result: {
            done: boolean;
            value: UpdateType;
        }) => void;
        reader.read().then(read = (result) => {
            if (result.done) {
                return;
            }

            onUpdate(result.value);
            reader.read().then(read);
        });

        return stream.cancel;
    } else {
        throw 'no response body';
    }
}