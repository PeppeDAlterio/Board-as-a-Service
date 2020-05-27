package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.exception.NotConnectedException;
import it.unina.sistemiembedded.utility.ui.UILongRunningHelper;
import it.unina.sistemiembedded.utility.ui.UISizeHelper;
import it.unina.sistemiembedded.utility.ui.stream.UIPrinterHelper;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SendMessageForm extends ClientJFrame {
    private JPanel mainPanel;
    private JTextField textFieldMessage;
    private JButton sendButton;
    JTextArea textAreaComunication;
    private JScrollPane scrollTextArea;

    public SendMessageForm(Client client) {
        super("Send message - Client - Board as a Service");
        UISizeHelper.setSize(this,0.5, 0.5);
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.scrollTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.setLocationRelativeTo(null);

        this.textAreaComunication.setEditable(false);
        this.textAreaComunication.setFont(new Font("courier", Font.BOLD, 12));

        sendButton.addActionListener(e -> {
                String message = textFieldMessage.getText();
                if (message.compareTo("") == 0) {
                    JOptionPane.showMessageDialog(null, "There are no messages", "", JOptionPane.WARNING_MESSAGE);
                } else {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date();
                    UIPrinterHelper.clientMessage("[ " + formatter.format(date) + " ]   " + message);
                    textFieldMessage.setText("");
                    UILongRunningHelper.<Exception>supplyAsync(this,"Sending message...",()->{
                        try {
                            client.sendTextMessage(message);
                        }catch (NotConnectedException ex){
                            return ex;
                        }
                        return null;
                    },result->{
                        if(result instanceof NotConnectedException){
                            JOptionPane.showMessageDialog(this,"Client not connected!","Error!",JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
        });


    }

}
