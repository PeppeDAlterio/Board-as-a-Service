package it.unina.sistemiembedded.boundary.client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RemoteDebugForm extends JFrame{
    private JPanel mainPanel;
    private JTextField textFieldgdbPort;
    private JButton debugButton;
    private JTextArea textAreaResponse;
    int gdbPort;

    public RemoteDebugForm() {
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        debugButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textFieldgdbPort.getText().compareTo("")==0) {
                    JOptionPane.showMessageDialog(null,"Insert a valid GDB port number!","",JOptionPane.ERROR_MESSAGE);
                }else{
                    gdbPort = Integer.parseInt(textFieldgdbPort.getText());
                    textAreaResponse.append("Starting remote gdb debug session on port :" + Integer.toString(gdbPort) + " ...\n");
                }
            }
        });
    }

    public static void main(String[] args) {
        RemoteDebugForm remoteDebugForm = new RemoteDebugForm();
    }
}
