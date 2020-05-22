package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.utility.ui.CustomOutputStream;
import it.unina.sistemiembedded.utility.ui.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;

public class RemoteDebugForm extends JFrame{
    private JPanel mainPanel;
    private JTextField textFieldgdbPort;
    private JButton debugButton;
    private JTextArea textAreaResponse;

    private PrintStream printStream;
    private int gdbPort;

    private void setSize(double height_inc,double weight_inc){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height *height_inc);
        int width = (int) (screenSize.width *weight_inc);
        this.setPreferredSize(new Dimension(width, height));
    }
    public RemoteDebugForm(Client client) {
        super();
        setSize(0.5,0.7);
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        printStream = new PrintStream(new CustomOutputStream(null,null,this.textAreaResponse,null,null));
        UIHelper.setPrintStream(printStream);
        debugButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textFieldgdbPort.getText().compareTo("")==0) {
                    JOptionPane.showMessageDialog(null,"Insert a valid GDB port number!","",JOptionPane.ERROR_MESSAGE);
                }else{
                    gdbPort = Integer.parseInt(textFieldgdbPort.getText());
                    UIHelper.cleintDebug("Starting remote gdb debug session on port :" + Integer.toString(gdbPort));
                    client.requestDebug(gdbPort);
                    //TODO : INFO AL CLIENT PER L'USO DEL DEGUB REMOTO
                }
            }
        });
    }

}
