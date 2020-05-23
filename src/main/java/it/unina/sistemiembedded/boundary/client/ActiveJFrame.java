package it.unina.sistemiembedded.boundary.client;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;


public class ActiveJFrame extends JFrame {
    private static Set<ActiveJFrame> activeFrame = new HashSet<>();
    private ActiveJFrame $this=this;


    public ActiveJFrame(String title){
        super(title);
        addActiveFrame(this);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                removeActiveFrame($this);
            }
        });
    }

    private static void addActiveFrame(ActiveJFrame frame){
        activeFrame.add(frame);
    }

    public static void removeActiveFrame(ActiveJFrame frame){
        activeFrame.remove(frame);
    }

    public static Set<ActiveJFrame> getActiveFrame(){return activeFrame;}

}
