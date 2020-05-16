package it.unina.sistemiembedded.boundary.client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SendMessageForm extends JFrame{
    private JPanel mainPanel;
    private JTextField textFieldMessage;
    private JButton sendButton;
    private JTextArea textAreaComunication;

    public SendMessageForm(){
        super();
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO : Code to send a message
                String message = textFieldMessage.getText();
                if(message.compareTo("")==0) {
                    JOptionPane.showMessageDialog(null, "There are no message", "", JOptionPane.WARNING_MESSAGE);
                }else {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date();
                    textAreaComunication.append("[ " + formatter.format(date) + " ]   " + message + "\n");
                    textFieldMessage.setText("");
                }
            }
        });
    }

    public static void main(String[] args) {
        SendMessageForm sendMessageForm = new SendMessageForm();
    }
}
