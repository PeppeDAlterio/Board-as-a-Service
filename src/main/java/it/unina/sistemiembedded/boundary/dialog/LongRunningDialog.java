package it.unina.sistemiembedded.boundary.dialog;

import it.unina.sistemiembedded.boundary.client.ActiveJFrame;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LongRunningDialog extends ActiveJFrame {

    private JPanel mainPanel;
    private JLabel messageLabel;
    private JProgressBar progressBar;

    public LongRunningDialog(String message, JFrame parent) {
        super("LongRunningDialog");
        this.constructor(message, parent);
        this.progressBar.setIndeterminate(true);
       // this.progressBar.setValue(100);
    }

    private void constructor(String message, JFrame parent) {

        setContentPane(mainPanel);
        messageLabel.setText(message);
        parent.setEnabled(false);
        this.setUndecorated(true);
        this.progressBar.setMinimum(0);
        this.progressBar.setMaximum(100);

        this.pack();

        this.setLocationRelativeTo(parent);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                parent.setEnabled(true);
                parent.requestFocus();
            }
        });

    }

}
