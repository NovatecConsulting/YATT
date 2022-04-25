import {Scaffold} from "../../components/scaffold/Scaffold";
// @ts-ignore
import styles from '@chatscope/chat-ui-kit-styles/dist/default/styles.min.css';
// @ts-ignore
import {MainContainer, ChatContainer, MessageList, Message, MessageInput} from '@chatscope/chat-ui-kit-react';
import {Card} from "@mui/material";
import {useEffect, useState} from "react";
import {rsocket} from "../../app/rsocket";
import {useParams} from "react-router-dom";
import dayjs from "dayjs";
import {useAppSelector} from "../../app/hooks";
import {selectCurrentUser} from "../auth/usersSlice";

export interface ChatMessage {
    userIdentifier: string;
    userName: string;
    timestamp: string;
    message: string;
}

export function ChatPage() {
    const {projectId} = useParams<{ projectId: string }>();
    const currentUser = useAppSelector(selectCurrentUser);
    const [messages, setMessages] = useState([] as ChatMessage[]);

    const handleSend = (innerHtml: String, textContent: String, innerText: String, nodes: NodeList) => {
        rsocket.fireAndForget(`projects.${projectId}.chat.send`, innerText);
    }

    useEffect(() => {
        const subscription = rsocket.subscribeUpdates<ChatMessage>(`projects.${projectId}.chat`, message => {
            setMessages(messages => messages.concat(message))
        });

        return function cleanup() {
            subscription.cancel();
        };
    }, [projectId]);

    return (
        <Scaffold>
            <Card sx={{width: 600, flex: "1 1 auto"}}>
                <MainContainer style={styles}>
                    <ChatContainer>
                        <MessageList>
                            {
                                messages.map(message => (
                                    <Message model={{
                                        direction: currentUser!!.identifier === message.userIdentifier ? "outgoing" : "incoming",
                                        position: "single",
                                        payload: <Message.CustomContent>
                                            <strong>{message.userName}</strong><br/>
                                            {message.message.replace(RegExp("^\""), "").replace(RegExp("\"$"), "")}<br/>
                                            <div style={{
                                                textAlign: "right",
                                                fontWeight: "lighter"
                                            }}>{dayjs(message.timestamp).format('LT')}</div>
                                        </Message.CustomContent>
                                    }}/>
                                ))
                            }
                        </MessageList>
                        <MessageInput attachButton={false} placeholder="Type message here" onSend={handleSend}/>
                    </ChatContainer>
                </MainContainer>
            </Card>
        </Scaffold>
    );
}