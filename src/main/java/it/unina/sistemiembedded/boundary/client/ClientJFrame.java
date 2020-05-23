package it.unina.sistemiembedded.boundary.client;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;


public abstract class ClientJFrame extends JFrame {

    /**
     * Set of active frames, useful to dispose every frame when server disconnects
     */
    private static Set<ClientJFrame> activeFrames = new HashSet<>();

    /**
     * Add frame to active frames set
     * @param frame ClientJFrame frame
     */
    private static void addActiveFrame(ClientJFrame frame){
        activeFrames.add(frame);
    }

    /**
     * Remove frame from active frames set
     * @param frame ClientJFrame frame to remove
     */
    public static void removeActiveFrame(ClientJFrame frame){
        activeFrames.remove(frame);
    }

    /**
     * Gets copy of active frames set
     * @return Set copy of active frames set
     */
    public static Set<ClientJFrame> getActiveFrames() {
        return new HashSet<>(activeFrames);
    }

    /**
     * Clear active frames set
     */
    public static void clearActiveFrames() {
        activeFrames.clear();
    }

    /**
     * JFrame constructor adds itself in active frames set
     * @param title String JFrame title
     */
    public ClientJFrame(String title){
        super(title);
        addActiveFrame(this);

        final ClientJFrame $this=this;
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                removeActiveFrame($this);
            }
        });
    }

}
