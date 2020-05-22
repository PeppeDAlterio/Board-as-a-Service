package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChoiseForm extends  JFrame{
    private JPanel mainPanel;
    private JLabel labelLAB;
    private JLabel labelBoard;
    private JButton sendMessageButton;
    private JButton remoteFlashButton;
    private JButton remoteDegubButton;
    private JLabel label_name;


    public ChoiseForm(Client client, String lab , String board ,String ip ,int port) {
        super();
        String s = labelLAB.getText();
        s=s.replace("[IP]",ip);
        s=s.replace("[PORT]",Integer.toString(port));
        s=s.replace("[LAB]",lab);
        this.labelLAB.setText(lab);
        this.labelBoard.setText(board);
        this.setContentPane(this.mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        label_name.setText(client.getName());
        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SendMessageForm sendMessageForm = new SendMessageForm(client);
            }
        });
        remoteDegubButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RemoteDebugForm remoteDebugForm = new RemoteDebugForm(client);
            }
        });
        remoteFlashButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RemoteFlashForm remoteFlashForm = new RemoteFlashForm(client);
            }
        });
    }
}
