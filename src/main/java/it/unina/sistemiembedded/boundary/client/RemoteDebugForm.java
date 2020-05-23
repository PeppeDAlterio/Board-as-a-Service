package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.utility.ui.stream.CustomOutputStream;
import it.unina.sistemiembedded.utility.ui.stream.UIPrinterHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;

public class RemoteDebugForm extends ClientJFrame {
    private JPanel mainPanel;
    private JTextField textFieldgdbPort;
    private JButton debugButton;
    private JTextArea textAreaResponse;
    private JButton finishDebugSessionButton;
    private JScrollPane scrollTextArea;

    private PrintStream printStream;
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
        this.setVisible(true);
        this.pack();
        this.setLocationRelativeTo(null);

        scrollTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.textAreaResponse.setEditable(false);
        textAreaResponse.setFont(new Font("courier", Font.BOLD, 12));
        printStream = new PrintStream(new CustomOutputStream(null, null, this.textAreaResponse, null, null));
        UIPrinterHelper.setPrintStream(printStream);

        debugButton.addActionListener(e -> {
            if (textFieldgdbPort.getText().compareTo("") == 0) {
                //TODO : Maggiori informazioni nel JoptionPane
                JOptionPane.showMessageDialog(null, "Insert a valid GDB port number!", "", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    gdbPort = Integer.parseInt(textFieldgdbPort.getText());
                    UIPrinterHelper.clientDebug("Starting remote GDB debug session on port : " + gdbPort + "\n");
                    client.requestDebug(gdbPort);
                    UIPrinterHelper.clientDebug("To correctly use the remote debbugger :");
                    UIPrinterHelper.clientDebug("\t1)  Open your STM32CubeIDE");
                    UIPrinterHelper.clientDebug("\t2)  Open 'Degub Configuration' settings ");
                    UIPrinterHelper.clientDebug("\t3)  In the 'Debbugger' section enable 'Connect to remote GDB server");
                    UIPrinterHelper.clientDebug("\t4)  Insert the server ip and the port specified above");
                    UIPrinterHelper.clientDebug("\t5)  Click on 'Apply' and then 'Degub' buttons");
                    UIPrinterHelper.clientDebug("\t6)  Start debbugging!\n ");
                } catch (NumberFormatException n) {
                    n.getMessage();
                    JOptionPane.showMessageDialog(null, "Port number must be an integer in the range of valid port values [ 0 , 65535 ]", "Invalid port number", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //TODO : JOptionPane per segnalare termine sessione di debug(o eventualmente annullare)
                super.windowClosing(e);
                client.requestStopDebug();
            }
        });
        finishDebugSessionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.requestStopDebug();
            }
        });
    }

}
