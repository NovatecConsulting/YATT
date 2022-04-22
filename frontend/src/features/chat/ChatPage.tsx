import {Scaffold} from "../../components/scaffold/Scaffold";
// @ts-ignore
import styles from '@chatscope/chat-ui-kit-styles/dist/default/styles.min.css';
// @ts-ignore
import {MainContainer, ChatContainer, MessageList, Message, MessageInput} from '@chatscope/chat-ui-kit-react';
import {Card} from "@mui/material";


export function ChatPage() {

    const handleSend = () => console.log("send pressed")

    return (
        <Scaffold>
            <Card sx={{width: 600, flex: "1 1 auto"}}>
                <MainContainer style={styles}>
                    <ChatContainer>
                        <MessageList>
                            <Message model={{
                                // message: "Hello my friend",
                                // sentTime: "just now",
                                // sender: "Joe",
                                direction: "incoming",
                                position: "single",
                                payload: <Message.CustomContent>
                                    <strong>Joe</strong><br/>
                                    Hello my friend<br/>
                                    <div style={{textAlign: "right", fontWeight: "lighter"}}>12:00</div>
                                </Message.CustomContent>
                            }}/>
                            <Message model={{
                                // message: "Hello",
                                // sentTime: "20:22",
                                // sender: "Me",
                                direction: "outgoing",
                                position: "single",
                                payload: <Message.CustomContent>
                                    {/*<strong>Joe</strong><br/>*/}
                                    Hello<br/>
                                    <div style={{textAlign: "right", fontWeight: "lighter"}}>12:01</div>
                                </Message.CustomContent>
                            }}/>
                            <Message model={{
                                direction: "incoming",
                                position: "single",
                                payload: <Message.CustomContent>
                                    <strong>Max</strong><br/>
                                    Hi<br/>
                                    <div style={{textAlign: "right", fontWeight: "lighter"}}>12:03</div>
                                </Message.CustomContent>
                            }}/>
                        </MessageList>
                        <MessageInput attachButton={false} placeholder="Type message here" onSend={handleSend}/>
                    </ChatContainer>
                </MainContainer>
            </Card>
        </Scaffold>
    );
}