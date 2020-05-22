package it.unina.sistemiembedded.boundary.server;

import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.CustomOutputStream;
import it.unina.sistemiembedded.utility.ui.UIHelper;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;

@Getter @Setter
public class ServerStartedForm extends JFrame {
    private JTextArea textAreaClientAction;
    private JTextArea textAreaClientComunication;
    private JLabel labelPortNumber;
    private JPanel mainPanel;
    private JLabel labelStartedOnPort;

    public PrintStream printStream;

    private void setSize(double height_inc,double weight_inc){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height *height_inc);
        int width = (int) (screenSize.width *weight_inc);
        this.setPreferredSize(new Dimension(width, height));
    }

    public ServerStartedForm(Server server){
        super();
        setSize(0.7,0.7);
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.textAreaClientAction.setEditable(false);
        this.textAreaClientComunication.setEditable(false);
        labelPortNumber.setText(Integer.toString(server.getPort()));
        labelStartedOnPort.setText(labelStartedOnPort.getText().replace("#SERVER#",server.getName()));
        printStream = new PrintStream(new CustomOutputStream(this.textAreaClientAction, this.textAreaClientComunication,null,null,null));
        UIHelper.setPrintStream(printStream);
    }
}
