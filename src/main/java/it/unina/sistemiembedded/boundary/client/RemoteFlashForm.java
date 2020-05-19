package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.utility.CustomOutputStream;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintStream;

public class RemoteFlashForm extends JFrame{
    private JPanel mainPanel;
    private JTextField textField1;
    private JButton startFlashButton;
    private JTextArea textAreaFlash;

    private PrintStream printStream;

    public RemoteFlashForm(Client client){
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();

        printStream = new PrintStream(new CustomOutputStream(null,null,null,this.textAreaFlash,null));

        startFlashButton.addActionListener(new ActionListener() {
            String elf_file = textAreaFlash.getText();
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO : Controlli su elf_file
                try {
                    client.flash(elf_file);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        System.setOut(printStream);
    }

}
