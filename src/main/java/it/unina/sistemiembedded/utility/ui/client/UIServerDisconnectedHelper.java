package it.unina.sistemiembedded.utility.ui.client;

import it.unina.sistemiembedded.boundary.client.ClientJFrame;
import it.unina.sistemiembedded.main.MainClientGUIForm;

import java.util.concurrent.Executors;

public class UIServerDisconnectedHelper {

    /**
     * Server disconnected event UI callback
     */
    public static void uiServerDisconnectedCallback() {
        Executors.newSingleThreadExecutor().execute(()->{
            for (ClientJFrame jFrame : ClientJFrame.getActiveFrames()) {
                try {
                    jFrame.dispose();
                }catch (Exception ignored){}
            }
            ClientJFrame.clearActiveFrames();
            new MainClientGUIForm();
        });
    }

}
