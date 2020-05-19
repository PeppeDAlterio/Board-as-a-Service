package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.utility.CustomOutputStream;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;

public class RemoteDebugForm extends JFrame{
    private JPanel mainPanel;
    private JTextField textFieldgdbPort;
    private JButton debugButton;
    private JTextArea textAreaResponse;

    private PrintStream printStream;
    private int gdbPort;


    public RemoteDebugForm(Client client) {
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        printStream = new PrintStream(new CustomOutputStream(null,null,this.textAreaResponse,null,null));
        System.setOut(printStream);
        debugButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textFieldgdbPort.getText().compareTo("")==0) {
                    JOptionPane.showMessageDialog(null,"Insert a valid GDB port number!","",JOptionPane.ERROR_MESSAGE);
                }else{
                    gdbPort = Integer.parseInt(textFieldgdbPort.getText());
                    System.out.println(RemoteDebugForm.class+"%Starting remote gdb debug session on port :" + Integer.toString(gdbPort));
                    client.debug(gdbPort);
                }
            }
        });
    }

}
