package it.unina.sistemiembedded.utility.ui;

import it.unina.sistemiembedded.boundary.client.ActiveJFrame;
import it.unina.sistemiembedded.main.MainClientGUIForm;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

public class UIHelperServerDisconnected {
    public static void serverDisconnected(){
        Set<ActiveJFrame> temp = new HashSet<>(ActiveJFrame.getActiveFrame());
        Executors.newSingleThreadExecutor().execute(()->{
            for (ActiveJFrame jFrame : temp) {
                try {
                    jFrame.dispose();
                }catch (Exception ignored){}
            }
            ActiveJFrame.getActiveFrame().clear();
            new MainClientGUIForm();
        });
    }
}
