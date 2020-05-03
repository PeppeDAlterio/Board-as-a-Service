package it.corsose.juartcommunic.net;

import it.corsose.juartcommunic.driver.COMDriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
    private Scanner scanner = new Scanner(System.in);

    private String serverIpAddress = "";
    private int serverPort = 1234;

    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;

    private Thread receiveThread;
    private Thread transmitThread;

    private COMDriver comDriver;

    private boolean initialized = false;

    //

    public Socket getSocket() {
        return socket;
    }

    public DataInputStream getDis() {
        return dis;
    }

    public DataOutputStream getDos() {
        return dos;
    }

    public Client(String serverIpAddress, int serverPort) {

        this.serverPort = serverPort;
        this.serverIpAddress = serverIpAddress;

    }

    // 25.85.9.170
    public void startClient() {

        try {

            this.socket = new Socket(InetAddress.getByName(this.serverIpAddress), this.serverPort);

            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());

            comDriver = new COMDriver(COMDriver.listPorts().get(0));

        } catch (IOException e) {

            System.err.println("Errore di connessione verso il server.");

        } catch (IllegalArgumentException e) {

            System.err.println("COM Port non connessa !");

            if(!this.socket.isClosed()) {
                try {
                    this.socket.close();
                } catch (IOException ignored) {}
            }

            return;

        }

        if(this.socket == null) {
            System.err.println("Impossibile stabilire una connessione al server " + serverIpAddress + ":" + serverPort);
            return;
        }

        System.out.println("Connessione avviata con socket: " + this.socket);

        transmitThread = new Thread( () -> {

            String msg;

            System.out.println("Inserisci il tuo nome: ");
            msg = scanner.nextLine();
            if(socket.isConnected()) {
                try {
                    dos.writeUTF(msg);
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            } else {
                System.err.println("Socket non connessa");
            }

//            while (socket!=null && socket.isConnected()) {
//
//                System.out.println("Please, type your message for the server: ");
//                msg = scanner.nextLine();
//                if(socket.isConnected()) {
//                    try {
//                        dos.writeUTF(msg);
//                    } catch (IOException e) {
//                        return;
//                    }
//                } else {
//                    System.err.println("Socket non connessa");
//                    return;
//                }
//
//            }

        });

        // readMessage thread
        receiveThread = new Thread( () -> {

                while (socket!=null && socket.isConnected()) {
                    try {
                        // read the message sent to this client
                        String msg = dis.readUTF();

                        System.out.println("Ho ricevuto: " + msg);

                        comDriver.writeLn(msg);

                        new Thread( () -> {

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            comDriver.consumeAllAvailableMessages().forEach(m -> {
                                try {
                                    this.dos.writeUTF(m);
                                } catch (IOException ignored) { }
                            });

                        }).start();


                    } catch (IOException e) {
                        System.err.println("Connessione al server interrotta");
                        try { dos.writeUTF(""); } catch (IOException ignored) { }
                        return;
                    } catch (NullPointerException e) {
                        return;
                    }
                }

        });

        transmitThread.start();
        receiveThread.start();

    }
}