package it.unina.sistemiembedded.boundary.server;

import it.unina.sistemiembedded.main.MainServerGUIForm;
import it.unina.sistemiembedded.model.ConnectedClient;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.ui.UILongRunningHelper;
import it.unina.sistemiembedded.utility.ui.UISizeHelper;
import it.unina.sistemiembedded.utility.ui.stream.CustomOutputStream;
import it.unina.sistemiembedded.utility.ui.stream.UIPrinterHelper;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    private JList<Object> listClientsConnected;
    private JButton buttonRefresh;
    private JButton boardSListButton;

    public PrintStream printStream;

    private DefaultListModel<Object> defaultListModel;

    private Server server;

    private JFrame $this=this;

    private void initClientsConnectedList(){
        defaultListModel = new DefaultListModel<Object>();
        UILongRunningHelper.runAsync(this,"Loading connected client's list",()->{
            List<ConnectedClient> connectedClients = server.listConnectedClients();
            if(connectedClients.size()!=0) {
                for (ConnectedClient connectedClient : connectedClients) {
                    defaultListModel.addElement(connectedClient);
                }
            }else{
                defaultListModel.addElement("No client connected.");
            }
        });
        listClientsConnected.setModel(defaultListModel);
    }



    public ServerStartedForm(Server server,JFrame parent) {
        super("Server console - Board as a Service");
        this.server = server;
        tabbedPane.setTitleAt(0,"Server log");
        tabbedPane.setTitleAt(1,"Clients communications");
        UISizeHelper.setSize(this,0.7, 0.7);
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.spAct.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.spComm.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.textAreaClientAction.setEditable(false);
        this.textAreaClientComunication.setEditable(false);
        this.textAreaClientComunication.setFont(new Font("courier", Font.BOLD, 12));
        this.textAreaClientAction.setFont(new Font("courier", Font.BOLD, 12));
        this.textAreaAssociatedBoard.setFont(new Font("courier",Font.BOLD,12));
        initClientsConnectedList();
        printStream = new PrintStream(new CustomOutputStream(this.textAreaClientAction, this.textAreaClientComunication, null, null, null));
        UIPrinterHelper.setPrintStream(printStream);
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> { textAreaAssociatedBoard.setText("");initClientsConnectedList();}, 0, 1, TimeUnit.MINUTES);


        listClientsConnected.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(e.getValueIsAdjusting())return;
                textAreaAssociatedBoard.setText("");
                if(listClientsConnected.getSelectedValue() instanceof ConnectedClient) {
                    if(((ConnectedClient) listClientsConnected.getSelectedValue()).getBoard().isPresent()){
                        textAreaAssociatedBoard.append(((ConnectedClient) listClientsConnected.getSelectedValue()).getBoard().get().toString()+"\n");
                    }else{
                        textAreaAssociatedBoard.append("No board associated to the selected client.\n");
                    }
                }
            }
        });

        buttonRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textAreaAssociatedBoard.setText("");
                initClientsConnectedList();
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choise=JOptionPane.showConfirmDialog($this,"This will shoutdown the server! Continue?","Stop server.",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
                if(choise==JOptionPane.YES_OPTION) {
                    dispose();
                    new MainServerGUIForm();
                }
            }
        });
        boardSListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                parent.setVisible(true);
            }
        });
    }

}
