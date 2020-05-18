package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.impl.ClientImpl;

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

    public ChoiseForm(ClientImpl client, String lab , String board) {
        super();
        this.labelLAB.setText(lab);
        this.labelBoard.setText(board);
        this.setContentPane(this.mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        label_name.setText(client.getName());
        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SendMessageForm sendMessageForm = new SendMessageForm();
            }
        });
        remoteDegubButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RemoteDebugForm remoteDebugForm = new RemoteDebugForm();
            }
        });
        remoteFlashButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RemoteFlashForm remoteFlashForm = new RemoteFlashForm();
            }
        });
    }
}
