package it.unina.sistemiembedded.utility;

import it.unina.sistemiembedded.utility.ui.UIHelper;

import javax.swing.*;
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
    public void write(int b){

        buffer.append((char)b);

        final String stringBuffer = buffer.toString();

        if ( stringBuffer.endsWith("\r") || stringBuffer.endsWith("\n") ) {
            if(stringBuffer.startsWith(UIHelper.TextArea.CLIENT_FLASH.getValue())){
                textAreaRemoteFlash.append(stringBuffer.substring(UIHelper.TextArea.CLIENT_FLASH.getValue().length())+"\n");
            }else if(stringBuffer.startsWith(UIHelper.TextArea.CLIENT_DEBUG.getValue())){
                textAreaRemoteDegubFormDebug.append(stringBuffer.substring(UIHelper.TextArea.CLIENT_DEBUG.getValue().length())+"\n");
            }else if(stringBuffer.startsWith(UIHelper.TextArea.CLIENT_MESSAGE.getValue())) {
                textAreaSendMessage.append(stringBuffer.substring(UIHelper.TextArea.CLIENT_MESSAGE.getValue().length())+"\n");
            }else if(stringBuffer.startsWith(UIHelper.TextArea.SERVER_ACTION.getValue())) {
                textAreaServerStartedFromClientAction.append(stringBuffer.substring(UIHelper.TextArea.SERVER_ACTION.getValue().length())+"\n");
            } else if(stringBuffer.startsWith(UIHelper.TextArea.SERVER_COMMUNICATION.getValue())){
                textAreaServerStartedFromClientComunication.append(stringBuffer.substring(UIHelper.TextArea.SERVER_COMMUNICATION.getValue().length())+"\n");
            }

            buffer = new StringBuilder();

        }
    }



}
