package it.unina.sistemiembedded.boundary.client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RemoteFlashForm extends JFrame{
    private JPanel mainPanel;
    private JTextField textField1;
    private JButton startFlashButton;
    private JTextArea textAreaFlash;

    public RemoteFlashForm(){
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();


        startFlashButton.addActionListener(new ActionListener() {
            String elf_file = textAreaFlash.getText();
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO : Flash operation
            }
        });
    }

    public static void main(String[] args) {
        RemoteFlashForm remoteFlashForm = new RemoteFlashForm();
    }
}
