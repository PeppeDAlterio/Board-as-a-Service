package it.unina.sistemiembedded.boundary.server;

import it.unina.sistemiembedded.server.impl.ServerImpl;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class SetSerialParamForm extends JFrame {
    private JTextField textFieldBaudRate;
    private JTextField textFieldNumData;
    private JTextField textFieldStopBit;
    private JButton startServerWhitThoseButton;
    private JPanel mainPanel;
    private JTextField textFieldParity;

    public SetSerialParamForm(ServerImpl server){
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        textFieldBaudRate.setText("115200");
        textFieldNumData.setText("8");
        textFieldStopBit.setText("1");
        textFieldParity.setText("NO_PARITY");
        startServerWhitThoseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    server.start();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                new ServerStartedForm(server);
            }
        });
    }
}
