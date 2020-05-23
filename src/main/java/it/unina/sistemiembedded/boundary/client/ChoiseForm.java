package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChoiseForm extends ActiveJFrame {
    private JPanel mainPanel;
    private JLabel labelLAB;
    private JLabel labelBoard;
    private JButton sendMessageButton;
    private JButton remoteFlashButton;
    private JButton remoteDegubButton;
    private JLabel label_name;
    private JButton requestAnotherBoardButton;

    private int closeForm = 1;

    public ChoiseForm(Client client, String lab, String board, String ip, int port,JFrame parent) {
        super("ChoiseForm");
        System.out.println(ActiveJFrame.getActiveFrame());
        String infoLab = labelLAB.getText();
        infoLab = infoLab.replace("[IP]", "[ "+ip);
        infoLab = infoLab.replace("[PORT]", Integer.toString(port)+" ]");
        infoLab = infoLab.replace("[LAB]", lab);
        this.labelLAB.setText(infoLab);
        this.labelBoard.setText(board);
        this.setContentPane(this.mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        label_name.setText(client.getName());
        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SendMessageForm(client);
            }
        });
        remoteDegubButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RemoteDebugForm(client);
            }
        });
        remoteFlashButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RemoteFlashForm(client);
            }
        });
        requestAnotherBoardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeForm =0;
                JOptionPane.showMessageDialog(null, "This operation will detach you from the current board", "Request another board", JOptionPane.INFORMATION_MESSAGE);
                client.requestReleaseBoard();
                dispose();
                new AttachBoardForm(client, ip, port);

            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                if(closeForm == 1) {
                    JOptionPane.showMessageDialog(null, "This operation will detach you from the current board", "Closing the current session...", JOptionPane.INFORMATION_MESSAGE);
                    client.requestReleaseBoard();
                    new AttachBoardForm(client, ip, port);
                }
            }
        });
    }

}
