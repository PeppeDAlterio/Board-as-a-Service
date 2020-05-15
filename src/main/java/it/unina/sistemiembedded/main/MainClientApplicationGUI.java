package it.unina.sistemiembedded.main;

import com.fazecast.jSerialComm.SerialPort;
import it.unina.sistemiembedded.net.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Scanner;

public class MainClientApplicationGUI extends JFrame {
    public  static String ipAddress = "localhost";
    public  static int port = 1235;
    public  String name;
    public  String board;
    public  int comPortNumber;

    public static final Scanner scanner = new Scanner(System.in);
    private JTextField textFieldName;
    private JTextField textFieldBoard;
    private JPanel Panel;
    private JButton buttonstartConnection;
    private JTextArea textAreaComPort;
    private JTextField textFieldComPort;
    private DefaultListModel defaultListModel = new DefaultListModel();

    private void InitTextAreaComPort(JTextArea textArea){
        List<SerialPort> ports = Client.listAvailableCOMPorts();
        for(int i=0;i<ports.size();i++){
            textArea.append("[ "+i+" ]   "+ports.get(i).getDescriptivePortName()+"\n");
        }
    }

    public MainClientApplicationGUI(){
        super();
        this.setContentPane(Panel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
        InitTextAreaComPort(this.textAreaComPort);
        textAreaComPort.setEditable(false);
        buttonstartConnection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comPortNumber = Integer.parseInt(textFieldComPort.getText());
                name = textFieldName.getText();
                board = textFieldBoard.getText();
                Client client = new Client(ipAddress, port);
                try {
                    client.startClient(comPortNumber, name,board);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }


    public static void main(String[] args) {
        if(args.length>0) {
            ipAddress = args[0];
        }

        if(args.length>1) {
            port = Integer.parseInt(args[1]);
        }

        MainClientApplicationGUI clientApplicationGUI = new MainClientApplicationGUI();

        boolean wrongNumber = false;
        int comPortNumber = -1;

        do {
            System.out.println("Seleziona una COM port specificando il numero associato");
            List<SerialPort> ports = Client.listAvailableCOMPorts();

            if(ports.isEmpty()) {
                System.err.println("Nessuna scheda rilevata!");
                return;
            }

            for (int i = 0; i < ports.size(); i++) {
                System.out.println("[" + i + "] " + ports.get(i).getDescriptivePortName());
            }

            try {
                comPortNumber = scanner.nextInt();

                wrongNumber = comPortNumber < 0 || comPortNumber >= ports.size();

            } catch (Exception e) {
                scanner.nextLine();
                wrongNumber = true;
            }

        } while(wrongNumber);

        /*
        Client client = new Client(ipAddress, port);
        try {
            client.startClient(comPortNumber,);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

}
