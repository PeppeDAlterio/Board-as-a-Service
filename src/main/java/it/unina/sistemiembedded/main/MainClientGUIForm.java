package it.unina.sistemiembedded.main;

import it.unina.sistemiembedded.boundary.client.AttachBoardForm;
import it.unina.sistemiembedded.boundary.client.ClientJFrame;
import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.client.impl.ClientImpl;
import it.unina.sistemiembedded.utility.ui.UILongRunningHelper;

import javax.swing.*;
import java.awt.*;
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

    private String nameClient = "Client" + (int) (Math.random() * 1000 + 1000);

    private String ipAddress;
    private int portNumber;
    private Client client;

    private int clickedFistTime = 0;

    private void setSize(double height_inc, double weight_inc) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height * height_inc);
        int width = (int) (screenSize.width * weight_inc);
        this.setPreferredSize(new Dimension(width, height));
    }

    public MainClientGUIForm() {
        super("Client - Board as a Server");
        System.out.println(ClientJFrame.getActiveFrames());
        setSize(0.3, 0.3);
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.textFieldName.setText(nameClient);
        startConnectionButton.addActionListener(e -> {
                ipAddress = textFieldIP.getText();
                portNumber = Integer.parseInt(textFieldPort.getText());
                String name = textFieldName.getText();
                if (name.compareTo("") == 0) {
                    name = nameClient;
                }
                client = new ClientImpl(name);
                UILongRunningHelper.<Boolean>supplyAsync(this,"Connecting...",()-> {
                    try {
                        client.connect(ipAddress,portNumber);
                        return true;
                    } catch (IOException ignored) {
                        return false;
                    }
                },result ->{
                    if(result){
                        new AttachBoardForm(client, ipAddress, portNumber);
                        dispose();
                    }else{
                        JOptionPane.showMessageDialog(this, "Can't connect to " + ipAddress + ":" + portNumber, "Connection error", JOptionPane.ERROR_MESSAGE);
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
