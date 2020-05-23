package it.unina.sistemiembedded.boundary.dialog;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LongRunningDialog extends JDialog {

    private JPanel contentPane;
    private JLabel messageLabel;


    public LongRunningDialog(String message, JFrame parent) {

        this.constructor(message, parent);

    }

    private void constructor(String message, JFrame parent) {

        setContentPane(contentPane);
        messageLabel.setText(message);

        parent.setEnabled(false);

        this.setUndecorated(true);

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
