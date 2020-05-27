package it.unina.sistemiembedded.utility.ui.client;

import it.unina.sistemiembedded.boundary.client.ClientJFrame;
import it.unina.sistemiembedded.main.MainClientGUIForm;

import javax.swing.*;
import java.util.concurrent.Executors;

public class UIServerDisconnectedHelper {

    public enum ServerDisconnectedOperation {
        /**
         * Restart the application
         */
        RESTART,
        /**
         * Close the applicatino
         */
        CLOSE,
        /**
         * No Operation
         */
        NOOP
    }

    private static ServerDisconnectedOperation DEFAULT_OPERATION = ServerDisconnectedOperation.RESTART;

    public static void setDefaultOperation(ServerDisconnectedOperation operation) {
        DEFAULT_OPERATION = operation;
    }

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

            switch (DEFAULT_OPERATION) {
                case CLOSE:
                    System.exit(0);
                    break;
                case RESTART:
                    MainClientGUIForm mainClientGUIForm = new MainClientGUIForm();
                    JOptionPane.showMessageDialog(mainClientGUIForm,
                            "Connection to server lost",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    break;

                case NOOP:
                default:
                    return;
            }

        });
    }

}
