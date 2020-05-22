package it.unina.sistemiembedded.utility;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

public class CustomOutputStream extends OutputStream {

    private StringBuilder buffer = new StringBuilder();

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

        final String stringBuffer = buffer.toString();

        if ( stringBuffer.endsWith("\r") || stringBuffer.endsWith("\n") ) {
            if(stringBuffer.startsWith(RedirectStream.TEXT_AREA_FLASH_CLIENT)){
                textAreaRemoteFlash.append(stringBuffer.substring(RedirectStream.TEXT_AREA_FLASH_CLIENT.length())+"\n");
            }else if(stringBuffer.startsWith(RedirectStream.TEXT_AREA_DEGUB_CLEINT)){
                textAreaRemoteDegubFormDebug.append(stringBuffer.substring(RedirectStream.TEXT_AREA_DEGUB_CLEINT.length())+"\n");
            }else if(stringBuffer.startsWith(RedirectStream.TEXT_AREA_SENDMESSAGE_CLIENT)) {
                textAreaSendMessage.append(stringBuffer.substring(RedirectStream.TEXT_AREA_SENDMESSAGE_CLIENT.length())+"\n");
            }else if(stringBuffer.startsWith(RedirectStream.TEXT_AREA_ACTION_SERVER)) {
                textAreaServerStartedFromClientAction.append(stringBuffer.substring(RedirectStream.TEXT_AREA_ACTION_SERVER.length())+"\n");
            } else if(stringBuffer.startsWith(RedirectStream.TEXT_AREA_COMUNICATION_SERVER)){
                textAreaServerStartedFromClientComunication.append(stringBuffer.substring(RedirectStream.TEXT_AREA_COMUNICATION_SERVER.length())+"\n");
            }

            buffer = new StringBuilder();

        }
    }



}
