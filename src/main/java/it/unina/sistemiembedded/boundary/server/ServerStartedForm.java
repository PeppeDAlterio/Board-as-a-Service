package it.unina.sistemiembedded.boundary.server;

import it.unina.sistemiembedded.server.impl.ServerImpl;

import javax.swing.*;

public class ServerStartedForm extends JFrame {
    private JTextArea textAreaClientAction;
    private JTextArea textAreaClientComunication;
    private JLabel labelPortNumber;
    private JPanel mainPanel;

    public ServerStartedForm(ServerImpl server){
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        labelPortNumber.setText(Integer.toString(server.getPort()));
    }
}
