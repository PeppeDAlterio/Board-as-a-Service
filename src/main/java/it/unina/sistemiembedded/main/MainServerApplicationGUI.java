
package it.unina.sistemiembedded.main;


import it.unina.sistemiembedded.boundary.SendMessageGUI;
import it.unina.sistemiembedded.net.Server;
import it.unina.sistemiembedded.utility.Constants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MainServerApplicationGUI extends JFrame {

    private static Server server;
    private static MainServerApplicationGUI serverApplicationGUI;
    private JPanel ServerPanel;

    private static final Scanner scanner = new Scanner(System.in);

    private JLabel labelPortNumber;
    private JTextArea TextAreaClientConnected;
    private JTextField textFieldListClient;
    private JLabel StroryClientConnection;
    private JLabel ServerStartLabel;
    private JLabel LabelListConnectedClient;
    private JButton buttongetClientSList;
    private JButton buttonsendMessageToAClient;
    private JButton buttonrequireARemoteFlash;
    private JButton buttonrequireARemoteDebug;

    public JTextArea getTextAreaClientConnected(){return this.TextAreaClientConnected;}
    public void setStoryClientConnection(String string){this.TextAreaClientConnected.append(string);}
    public void setListofclientconnected(String string){this.textFieldListClient.setText(string);}
    public void setlabelPortNumber (String portNumer){this.labelPortNumber.setText(portNumer);}

    public MainServerApplicationGUI(){
        super();
        this.setContentPane(this.ServerPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
        this.textFieldListClient.setEditable(false);
        this.TextAreaClientConnected.setEditable(false);

        buttonsendMessageToAClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 new SendMessageGUI();
            }
        });
        buttongetClientSList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setListofclientconnected(printClients().toString());
            }
        });
    }

    public static void main(String[] args) throws IOException {
        serverApplicationGUI = new MainServerApplicationGUI();

        int port=1235;
        server = new Server(port,serverApplicationGUI);
        server.startServer();
        serverApplicationGUI.setlabelPortNumber(Integer.toString(port));

        while (server.isRunning()) {

            int selection = printMenuAndGetSelection();

            switch (selection) {

                case 3:
                    try {
                        remoteFlash();
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    break;

                case 9:
                    try {
                        remoteDebug();
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    break;

                case 0: default:
                    server.stopServer();
                    break;

            }

            enterToContinue();

        }

    }

    private static int printMenuAndGetSelection() {
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");

        System.out.println("====================================");
        System.out.println("1. Lista client");
        System.out.println("2. Invio messagio a client");
        System.out.println("3. Flash remoto");
        System.out.println("9. Debug remoto");
        System.out.println("0. Esci");
        System.out.println("====================================");
        int selection = scanner.nextInt();
        scanner.nextLine();

        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
        return selection;
    }

    private static ArrayList<String> printClients() {
        ArrayList<String> temp = new ArrayList<>();
        System.out.println(server.getClients());
        if(server.getClients().size()!=0) {
            for (int i = 0; i < server.getClients().size(); i++)
                temp.add(server.getClients().get(i).toString() + "\n");
        }else{
            temp.add("Nessun client connesso");
        }
        return temp;
    }

    public static void sendMessageToClient(long id , String msg) {

        String clientName = server.getClientNameById(id);
        if(clientName==null) {
            JOptionPane.showMessageDialog(null,"Client non trovato");
            throw new IllegalArgumentException("Client non trovato");
        }
        try {
            server.sendMessage(id, msg);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null,"Client non trovato o non connesso");
            throw new IllegalArgumentException("Client non trovato o non connesso");
        }
        JOptionPane.showMessageDialog(null,"Messaggio '" + msg + "' inviato al client id " + id + ".");
    }

    public static void remoteFlash() throws IOException {

        System.out.println("Inserisci la path al file .ELF da flashare: ");
        String filePath = scanner.nextLine();

        filePath = filePath.trim();

        if(!new File(filePath).exists()) {
            throw new IllegalArgumentException("Il file specificato non esiste");
        }

        if(!filePath.endsWith(".elf")) {
            throw new IllegalArgumentException("Il file non ha estensione .ELF");
        }

        long id = selectClientId();

        server.sendMessage(id, Constants.BEGIN_OF_REMOTE_FLASH);
        server.sendFile(id, filePath);

    }

    public static void remoteDebug() {

        long id = selectClientId();

        String port;
        System.out.println("Inserisci un porto per il debug remoto:");
        System.out.flush();
        port = scanner.nextLine();

        server.sendMessage(id, Constants.BEGIN_OF_DEBUG);
        server.sendMessage(id, port);

        System.out.println("\nLa sessione di debug verrà avviata a breve...");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) { }

    }

    private static long selectClientId() {

        if(server.getClients().isEmpty()) {
            JOptionPane.showMessageDialog(null,"Nessun client connesso");
            throw new RuntimeException("Nessun client connesso");
        }

        long id;
        System.out.println(server.getClients());
        System.out.print("\nInserisci id client: ");
        System.out.flush();
        try {
            id = scanner.nextLong();
            scanner.nextLine();
        } catch (Exception e) {
            scanner.nextLine();
            JOptionPane.showMessageDialog(null,"Client non trovato");
            throw new IllegalArgumentException("Client non trovato");
        }

        return id;
    }

    private static void enterToContinue() {
        System.out.println("Premi invio per continuare...");
        System.out.flush();
        scanner.nextLine();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}

