package it.unina.sistemiembedded.boundary.server;

import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.ui.UISizeHelper;
import it.unina.sistemiembedded.utility.ui.stream.CustomOutputStream;
import it.unina.sistemiembedded.utility.ui.stream.UIPrinterHelper;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;

@Getter
@Setter
public class ServerStartedForm extends JFrame {
    private JTextArea textAreaClientAction;
    private JTextArea textAreaClientComunication;
    private JPanel mainPanel;
    private JScrollPane spAct;
    private JScrollPane spComm;
    private JTabbedPane tabbedPane;
    private JTextArea textAreaAssociatedBoard;
    private JList listClientsConnected;
    private JButton buttonRefresh;

    public PrintStream printStream;

    private DefaultListModel defaultListModel;


    private void initClientsConnectedList(){
        defaultListModel = new DefaultListModel();
        Object obj; //server.getConnectedClient();
        /*for(){
            defaultListModel.add(obj);
        }
        listClientConnected.setModel(defaultListModel);
        */
    }



    public ServerStartedForm(Server server,JFrame parent) {
        super("Server console - Board as a Service");
        tabbedPane.setTitleAt(0,"Server log");
        tabbedPane.setTitleAt(1,"Clients communications");
        UISizeHelper.setSize(this,0.7, 0.7);
        this.setContentPane(mainPanel);
        //this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.spAct.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.spComm.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.textAreaClientAction.setEditable(false);
        this.textAreaClientComunication.setEditable(false);
        this.textAreaClientComunication.setFont(new Font("courier", Font.BOLD, 12));
        this.textAreaClientAction.setFont(new Font("courier", Font.BOLD, 12));
        printStream = new PrintStream(new CustomOutputStream(this.textAreaClientAction, this.textAreaClientComunication, null, null, null));
        UIPrinterHelper.setPrintStream(printStream);


        //initClientsConnectedList();







        listClientsConnected.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                textAreaAssociatedBoard.removeAll();

            }
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //TODO : JOptionPane per segnalare termine sessione di debug(o eventualmente annullare)
                super.windowClosing(e);
                setVisible(false);
                parent.setVisible(true);
            }
        });


    }

}
