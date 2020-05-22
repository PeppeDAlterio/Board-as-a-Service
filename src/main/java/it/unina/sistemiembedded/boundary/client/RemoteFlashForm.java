package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.utility.ui.CustomOutputStream;
import it.unina.sistemiembedded.utility.ui.UIHelper;

import javax.swing.*;
import java.awt.*;
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
    private void setSize(double height_inc,double weight_inc){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height *height_inc);
        int width = (int) (screenSize.width *weight_inc);
        this.setPreferredSize(new Dimension(width, height));
    }
    public RemoteFlashForm(Client client){
        super();
        setSize(0.5,0.5);
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
                    client.requestFlash(elf_file);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        UIHelper.setPrintStream(printStream);
    }

}
