package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.utility.ui.UILongRunningHelper;
import it.unina.sistemiembedded.utility.ui.stream.UIPrinterHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RemoteDebugForm extends ClientJFrame {
    private JPanel mainPanel;
    private JTextField textFieldgdbPort;
    private JButton debugButton;
    JTextArea textAreaResponse;
    private JButton finishDebugSessionButton;
    private JScrollPane scrollTextArea;


    private int gdbPort;


    //private int debugStartedFirstTime=0;

    private void setSize(double height_inc, double weight_inc) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height * height_inc);
        int width = (int) (screenSize.width * weight_inc);
        this.setPreferredSize(new Dimension(width, height));
    }

    public RemoteDebugForm(Client client) {
        super("Debug - Client - Board as a Service");
        setSize(0.5, 0.7);
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //this.setVisible(true);
        this.pack();
        this.setLocationRelativeTo(null);

        scrollTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.textAreaResponse.setEditable(false);
        textAreaResponse.setFont(new Font("courier", Font.BOLD, 12));

        debugButton.addActionListener(e -> {
            if (textFieldgdbPort.getText().compareTo("") == 0) {
                //TODO : Maggiori informazioni nel JoptionPane
                JOptionPane.showMessageDialog(this, "Insert a valid GDB port number!", "", JOptionPane.ERROR_MESSAGE);
            } else {
                    UILongRunningHelper.<Exception>supplyAsync(this, "Starting GDB debug session started on port :" + gdbPort, () -> {
                        try {
                            gdbPort = Integer.parseInt(textFieldgdbPort.getText());
                            UIPrinterHelper.clientDebug("Remote GDB debug session started on port : " + gdbPort + "\n");
                            client.requestDebug(gdbPort);
                        }catch (NumberFormatException ex){
                            return ex;
                        }catch (IllegalArgumentException ex){
                            return ex;
                        }
                        return null;
                    },result ->{
                        if(result instanceof IllegalArgumentException){
                            JOptionPane.showMessageDialog(this,"","Error!",JOptionPane.ERROR_MESSAGE);
                        }else if(result instanceof NumberFormatException){
                            JOptionPane.showMessageDialog(null, "Port number must be an integer in the range of valid port values [ 0 , 65535 ]", "Invalid port number", JOptionPane.ERROR_MESSAGE);

                        }else{
                            UIPrinterHelper.clientDebug("To correctly use the remote debbugger :");
                            UIPrinterHelper.clientDebug("\t1)  Open your STM32CubeIDE");
                            UIPrinterHelper.clientDebug("\t2)  Open 'Degub Configuration' settings ");
                            UIPrinterHelper.clientDebug("\t3)  In the 'Debbugger' section enable 'Connect to remote GDB server");
                            UIPrinterHelper.clientDebug("\t4)  Insert the server ip and the port specified above");
                            UIPrinterHelper.clientDebug("\t5)  Click on 'Apply' and then 'Degub' buttons");
                            UIPrinterHelper.clientDebug("\t6)  Start debbugging!\n ");
                        }
                    });
            }
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //TODO : JOptionPane per segnalare termine sessione di debug(o eventualmente annullare)
                super.windowClosing(e);
                UIPrinterHelper.clientDebug("Remote GDB debug session on port : " + gdbPort + " finished.\n");
                client.requestStopDebug();
            }
        });
        finishDebugSessionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UIPrinterHelper.clientDebug("Remote GDB debug session on port : " + gdbPort + " finished.\n");
                client.requestStopDebug();
                setVisible(false);
            }
        });
    }

}
