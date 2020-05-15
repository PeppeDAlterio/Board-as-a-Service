package it.unina.sistemiembedded.boundary;

import it.unina.sistemiembedded.main.MainServerApplicationGUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SendMessageGUI extends JFrame{
    private JLabel labelTypeMessageHere;
    private JTextField textFieldTextToBeSend;
    private JButton buttonSendMessage;
    private JPanel Panel;
    private JTextField textFieldClientID;
    private JLabel labelInsertClientID;

    public JTextField getTextFieldClientID() { return textFieldClientID; }

    public JLabel getLabelInsertClientID() {
        return labelInsertClientID;
    }

    public SendMessageGUI(){
        super();
        this.setContentPane(this.Panel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();
        this.setVisible(true);
        this.textFieldTextToBeSend.setEditable(true);
        this.textFieldClientID.setEditable(true);


        buttonSendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long id = Integer.parseInt(textFieldClientID.getText());
                String msg = textFieldTextToBeSend.getText();
                MainServerApplicationGUI.sendMessageToClient(id,msg);
                try {
                    Thread.sleep(2000);
                } catch (IllegalArgumentException ex) {
                    System.err.println(ex.getMessage());
                    JOptionPane.showMessageDialog(null,"Error");
                }catch (InterruptedException ignored){
                    JOptionPane.showMessageDialog(null,"Error");
                }
            }
        });
    }
}
