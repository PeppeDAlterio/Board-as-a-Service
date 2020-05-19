package it.unina.sistemiembedded.utility;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

public class CustomOutputStream extends OutputStream {

    private StringBuilder buffer = new StringBuilder();
    private String subBuffer;

    private final JTextArea textAreaServerStartedFromClientAction;
    private final JTextArea textAreaServerStartedFromClientComunication;
    private final JTextArea textAreaRemoteDegubFormDebug;
    private final JTextArea textAreaRemoteFlash;
    private final JTextArea textAreaSendMessage;


    public CustomOutputStream(JTextArea textAreaServerStartedFromClientAction, JTextArea textAreaServerStartedFromClientComunication ,
                              JTextArea textAreaRemoteDegubFormDebug ,JTextArea textAreaRemoteFlash,JTextArea textAreaSendMessage) {
        this.textAreaServerStartedFromClientAction =  textAreaServerStartedFromClientAction;
        this.textAreaServerStartedFromClientComunication = textAreaServerStartedFromClientComunication;
        this.textAreaRemoteDegubFormDebug = textAreaRemoteDegubFormDebug;
        this.textAreaRemoteFlash = textAreaRemoteFlash;
        this.textAreaSendMessage = textAreaSendMessage;
    }

    @Override
    public void write(int b) throws IOException {
        buffer.append((char)b);
        if ( buffer.toString().endsWith("\r") || buffer.toString().endsWith("\n") ) {
            if(buffer.toString().contains(RedirectStream.TEXT_AREA_FLASH_CLIENT)){
                subBuffer = buffer.toString().substring(buffer.indexOf("%") + 1, buffer.length());
                textAreaRemoteFlash.append(subBuffer+"\n");
            }else if(buffer.toString().contains(RedirectStream.TEXT_AREA_DEGUB_CLEINT)){
                subBuffer = buffer.toString().substring(buffer.indexOf("%") + 1, buffer.length());
                textAreaRemoteDegubFormDebug.append(subBuffer+"\n");
            }else if(buffer.toString().contains(RedirectStream.TEXT_AREA_SENDMESSAGE_CLIENT)) {
                subBuffer = buffer.toString().substring(buffer.indexOf("%") + 1, buffer.length());
                textAreaSendMessage.append(subBuffer+"\n");
            }else if(buffer.toString().contains(RedirectStream.TEXT_AREA_ACTION_SERVER)) {
                subBuffer = buffer.toString().substring(buffer.indexOf("%")+1,buffer.length());
                textAreaServerStartedFromClientAction.append(subBuffer+"\n");
            } else if(buffer.toString().contains(RedirectStream.TEXT_AREA_COMUNICATION_SERVER)){
                subBuffer = buffer.toString().substring(buffer.indexOf("%")+1,buffer.length());
                textAreaServerStartedFromClientComunication.append(subBuffer+"\n");
            }
            buffer = new StringBuilder();
            subBuffer = "";
        }
    }



}
