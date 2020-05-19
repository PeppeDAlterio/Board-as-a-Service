package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.utility.CustomOutputStream;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SendMessageForm extends JFrame{
    private JPanel mainPanel;
    private JTextField textFieldMessage;
    private JButton sendButton;
    private JTextArea textAreaComunication;

    private PrintStream printStream;

    public SendMessageForm(Client client){
        super();
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();

        printStream = new PrintStream(new CustomOutputStream(null,null,null,null,this.textAreaComunication));
        System.setOut(printStream);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = textFieldMessage.getText();
                if(message.compareTo("")==0) {
                    JOptionPane.showMessageDialog(null, "There are no message", "", JOptionPane.WARNING_MESSAGE);
                }else {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date();
                    System.out.println(SendMessageForm.class +"%"+"[ "+formatter.format(date)+" ]   "+message);
                    textFieldMessage.setText("");
                    client.sendTextMessage(message);
                }
            }
        });



    }

}
