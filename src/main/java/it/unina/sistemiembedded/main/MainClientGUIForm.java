package it.unina.sistemiembedded.main;

import it.unina.sistemiembedded.boundary.client.AttachBoardForm;
import it.unina.sistemiembedded.boundary.client.ClientJFrame;
import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.client.impl.ClientImpl;
import it.unina.sistemiembedded.utility.ui.UILongRunningHelper;
import it.unina.sistemiembedded.utility.ui.UISizeHelper;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class MainClientGUIForm extends ClientJFrame {
    private JPanel mainPanel;
    private JLabel labelName;
    private JLabel labelIP;
    private JLabel labelPort;
    private JTextField textFieldName;
    private JTextField textFieldIP;
    private JTextField textFieldPort;
    private JPanel panel;
    private JButton startConnectionButton;

    private String nameClient = "Client-" + (int) (Math.random() * 1000 + 1000);

    private String ipAddress;
    private int portNumber;
    private static Client client;

    private int clickedFistTime = 0;


    public MainClientGUIForm() {
        super("Client - Board as a Service");
        UISizeHelper.setSize(this,0.3, 0.3);
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.textFieldName.setText(nameClient);

        if(client!=null && client.isConnected()) {
            client.disconnect();
        }

        startConnectionButton.addActionListener(e -> {
            ipAddress = textFieldIP.getText();
            portNumber = Integer.parseInt(textFieldPort.getText());
            String name = textFieldName.getText();
            if (name.compareTo("") == 0) {
                name = nameClient;
            }
            client = new ClientImpl(name);
            UILongRunningHelper.<Exception>supplyAsync(this, "Connecting...", () -> {
                try {
                    client.connect(ipAddress, portNumber);
                }catch (IllegalArgumentException ex){
                    return ex;
                } catch (IOException ex) {
                    return ex;
                }
                return null;
            }, result -> {
                if (result instanceof IOException) {
                    JOptionPane.showMessageDialog(this, "Can't connect to '" + ipAddress + ":" + portNumber +"'", "Connection error", JOptionPane.ERROR_MESSAGE);
                } else if(result instanceof  IllegalArgumentException){
                    JOptionPane.showMessageDialog(this, "Port number must be an integer in the range of valid port values [ 0 , 65535 ]", "Invalid port number", JOptionPane.ERROR_MESSAGE);
                } else {
                    new AttachBoardForm(client, ipAddress, portNumber);
                    dispose();
                }
            });

        });

        textFieldName.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (clickedFistTime == 0) {
                    textFieldName.setText("");
                    clickedFistTime++;
                }
            }
        });
    }

    public static void main(String[] args) {
        new MainClientGUIForm();
    }

}
