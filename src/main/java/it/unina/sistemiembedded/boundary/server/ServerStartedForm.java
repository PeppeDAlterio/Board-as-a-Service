package it.unina.sistemiembedded.boundary.server;

import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.CustomOutputStream;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.io.PrintStream;

@Getter @Setter
public class ServerStartedForm extends JFrame {
    private JTextArea textAreaClientAction;
    private JTextArea textAreaClientComunication;
    private JLabel labelPortNumber;
    private JPanel mainPanel;
    private JLabel labelStartedOnPort;

    public PrintStream printStream;

    public void setTexArea(String string){textAreaClientAction.append(string+"\n");}

    public ServerStartedForm(Server server){

        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.textAreaClientAction.setEditable(false);
        this.textAreaClientComunication.setEditable(false);
        labelPortNumber.setText(Integer.toString(server.getPort()));
        labelStartedOnPort.setText(labelStartedOnPort.getText().replace("#SERVER#",server.getName()));
        printStream = new PrintStream(new CustomOutputStream(this.textAreaClientAction, this.textAreaClientComunication,null,null,null));
        System.setOut(printStream);
    }
}
