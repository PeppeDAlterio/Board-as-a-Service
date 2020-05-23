package it.unina.sistemiembedded.boundary.frame;

import javax.swing.*;
import java.awt.*;

public abstract class ScreenCenteredJFrame extends JFrame {

    public ScreenCenteredJFrame(String title) throws HeadlessException {
        super(title);
        this.setLocationRelativeTo(null);
    }

    public ScreenCenteredJFrame() throws HeadlessException {
        this.setLocationRelativeTo(null);
    }
}
