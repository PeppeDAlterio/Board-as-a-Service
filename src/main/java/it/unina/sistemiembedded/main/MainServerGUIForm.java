package it.unina.sistemiembedded.main;

import it.unina.sistemiembedded.boundary.server.SetSerialParamForm;
import it.unina.sistemiembedded.server.impl.ServerImpl;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainServerGUIForm extends JFrame {
    private JPanel MainPanel;
    private JTextField textFieldName;
    private JList list1;
    private JButton customSerialParametersButton;

    private ServerImpl server;

    public MainServerGUIForm(){
        super();
        this.setContentPane(MainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setTitle("Board as a Service Server Application");
        //TODO : Inizializzare la lista delle comPort

        customSerialParametersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name_server = textFieldName.getText();
                if(name_server.compareTo("")==0)
                    name_server = "SERVER";
                server = new ServerImpl(name_server);
                new SetSerialParamForm(server);

            }
        });
    }

    public static void main(String[] args) {
        new MainServerGUIForm();
    }
}
