package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.utility.ui.CustomOutputStream;
import it.unina.sistemiembedded.utility.ui.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SendMessageForm extends ActiveJFrame {
    private JPanel mainPanel;
    private JTextField textFieldMessage;
    private JButton sendButton;
    private JTextArea textAreaComunication;
    private JScrollPane scrollTextArea;

    private PrintStream printStream;

    private void setSize(double height_inc, double weight_inc) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height * height_inc);
        int width = (int) (screenSize.width * weight_inc);
        this.setPreferredSize(new Dimension(width, height));
    }

    public SendMessageForm(Client client) {
        super("SendMessageForm");
        setSize(0.5, 0.5);
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        scrollTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.textAreaComunication.setEditable(false);
        this.textAreaComunication.setFont(new Font("courier", Font.BOLD, 12));

        printStream = new PrintStream(new CustomOutputStream(null, null, null, null, this.textAreaComunication));
        UIHelper.setPrintStream(printStream);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = textFieldMessage.getText();
                if (message.compareTo("") == 0) {
                    JOptionPane.showMessageDialog(null, "There are no message", "", JOptionPane.WARNING_MESSAGE);
                } else {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date();
                    UIHelper.clientMessage("[ " + formatter.format(date) + " ]   " + message);
                    textFieldMessage.setText("");
                    client.sendTextMessage(message);
                }
            }
        });


    }

}
