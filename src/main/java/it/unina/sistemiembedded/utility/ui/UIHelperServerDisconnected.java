package it.unina.sistemiembedded.utility.ui;

import it.unina.sistemiembedded.boundary.client.ActiveJFrame;

import java.util.HashSet;
import java.util.Set;

public class UIHelperServerDisconnected {
    public static void serverDisconnected(){
        Set<ActiveJFrame> temp = new HashSet<>(ActiveJFrame.getActiveFrame());
        for (ActiveJFrame jFrame : temp) {
            try {
                jFrame.dispose();
            }catch (Exception ignored){}
        }
        ActiveJFrame.getActiveFrame().clear();
    }
}
