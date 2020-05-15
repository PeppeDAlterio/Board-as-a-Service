package it.unina.sistemiembedded.net;

import com.fazecast.jSerialComm.SerialPort;
import it.unina.sistemiembedded.boarddriver.COMDriver;
import it.unina.sistemiembedded.net.file.SocketFileHelper;
import it.unina.sistemiembedded.utility.Constants;
import it.unina.sistemiembedded.utility.SystemHelper;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client
{
    private final Scanner scanner = new Scanner(System.in);

    private String serverIpAddress = "";
    private int serverPort = 1234;

    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;

    private COMDriver comDriver;

    private boolean running = false;

    private Process debugProcess = null;

    //

    public static List<SerialPort> listAvailableCOMPorts() {
        return COMDriver.listPorts();
    }

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

    public void startClient(int comPortNumber,String name,String board) throws IOException {

        try {

            this.socket = new Socket(InetAddress.getByName(this.serverIpAddress), this.serverPort);

            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());

            comDriver = new COMDriver(COMDriver.listPorts().get(comPortNumber));

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,"Errore di connessione verso il server");
            System.err.println("Errore di connessione verso il server.");

            throw e;

        } catch (IllegalArgumentException e) {

            System.err.println("COM Port non connessa !");

            if(this.socket!=null && !this.socket.isClosed()) {
                try {
                    this.socket.close();
                } catch (IOException ignored) {}
            }

            throw e;

        }

        if(this.socket == null) {
            throw new IllegalArgumentException("Impossibile stabilire una connessione al server " + serverIpAddress + ":" + serverPort);
        }

        System.out.println("Connessione avviata con socket: " + this.socket);

        this.running = true;

        // Thread for handshake
        new Thread(() -> {

            System.out.println("Inserisci il tuo nome: ");
            //scanner.nextLine();
            System.out.println("Inserisci il tipo di board che si vuole mettere a disposizione: ");
            //board = scanner.nextLine();
            if (socket.isConnected()) {
                try {
                    dos.writeUTF(name);
                    dos.writeUTF(board);
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            } else {
                System.err.println("Socket non connessa");
                stopClient();
            }

        }).start();

        // readMessage thread
        // read the message sent to this client
        waitForMessagesAsync();

    }

    public void stopClient() {
        this.running = false;
        this.comDriver.closeCommunication();
    }

    public boolean isRunning() {return this.running;}

    private void consumeAndSendCOMBufferAsync() {
        new Thread(this::consumeAndSendCOMBuffer).start();
    }

    private void consumeAndSendCOMBuffer() {

        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        comDriver.consumeAllAvailableMessages().forEach(m -> {
            try {
                this.dos.writeUTF(m);
            } catch (IOException ignored) {
            }
        });

    }

    private void waitForMessagesAsync() {
        new Thread(() -> {

            while (socket != null && socket.isConnected()) {
                try {
                    // read the message sent to this client
                    String msg = dis.readUTF();

                    if (msg.equals(Constants.BEGIN_OF_REMOTE_FLASH)) {

                        killActiveDebugSession();

                        System.out.println("Il server ha richiesto un flash remoto...");
                        String receivedFile = SocketFileHelper.receiveFile(dis, ".elf");
                        System.out.println("...trasferimento file ELF completato.");

                        SystemHelper.runCommandAndPrintOutput(
                                "."+Constants.STM_PROGRAMMER_PATH+Constants.STM_PROGRAMMER_EXE_NAME +
                                        " -c port=SWD -d " + receivedFile + " --start"
                        );

                        dos.writeUTF(Constants.END_OF_REMOTE_FLASH);

                        consumeAndSendCOMBufferAsync();

                    } else if(msg.equals(Constants.BEGIN_OF_DEBUG)) {

                        killActiveDebugSession();

                        int port = 0;
                        try {
                            port = Integer.parseInt(dis.readUTF());
                        } catch (NumberFormatException e) {
                            System.err.println("Ricevuto porto non valido: " + port);
                            continue;
                        }

                        System.out.println("Il server ha richiesto una sessione di debug remota sul porto "+ port + " ...");

                        final int finalPort = port;
                        new Thread( () -> {

                            try {
                                this.debugProcess = SystemHelper.remoteDebug(finalPort, dos);
                            } catch (IOException e) {
                                e.printStackTrace();
                                if(this.debugProcess != null) {
                                    this.debugProcess.destroyForcibly();
                                    try {
                                        this.debugProcess.waitFor();
                                    } catch (InterruptedException ignored) {
                                    }
                                }
                            }

                            System.out.println("\nSessione di debug terminata.");

                        }).start();

                    } else {

                        System.out.println("Ho ricevuto: " + msg);

                        comDriver.writeLn(msg);

                        consumeAndSendCOMBufferAsync();

                    }


                } catch (IOException e) {
                    System.err.println("Connessione al server interrotta");
                    try {
                        dos.writeUTF("");
                    } catch (IOException ignored) {
                    }
                    break;
                } catch (NullPointerException e) {
                    break;
                }
            }

            stopClient();

        }).start();
    }

    private void killActiveDebugSession() throws IOException {
        if(this.debugProcess!=null && this.debugProcess.isAlive()) {

            synchronized (dos) {
                dos.writeUTF("E' già in esecuzione una sessione di debug. Cancello la precedente...");
            }
            try {
                this.debugProcess.destroyForcibly();
                this.debugProcess.waitFor();
            } catch (Exception ignored) {}

            synchronized (dos) {
                dos.writeUTF("La precedente sessione di debug è stata terminata.");
            }

        }
    }

}