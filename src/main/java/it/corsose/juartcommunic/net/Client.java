package it.corsose.juartcommunic.net;

import it.corsose.juartcommunic.driver.COMDriver;

import java.io.*;
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

        });

        // readMessage thread
        receiveThread = new Thread( () -> {

                while (socket!=null && socket.isConnected()) {
                    try {
                        // read the message sent to this client
                        String msg = dis.readUTF();

                        if(msg.equals("--- BEGIN OF FILE TX ---")) {

                            String receivedFile = receiveFile();
                            runCommandAndPrintOutput(".\\tools\\STM32CubeProgrammer\\bin\\STM32_Programmer_CLI.exe -c port=SWD -d " + receivedFile + " --start");

                        } else {

                            System.out.println("Ho ricevuto: " + msg);

                            comDriver.writeLn(msg);

                            new Thread(() -> {

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                comDriver.consumeAllAvailableMessages().forEach(m -> {
                                    try {
                                        this.dos.writeUTF(m);
                                    } catch (IOException ignored) {
                                    }
                                });

                            }).start();

                        }


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

    private String receiveFile() throws IOException {

        String filename = dis.readUTF();

        filename = filename.trim();

        File newFile = new File("received/" + filename);

        if(newFile.exists() &&!newFile.delete()) {
            throw new IllegalArgumentException("Esiste già un file col nome '" + filename + "' e non è possibile cancellarlo.");
        }

        if(!newFile.createNewFile()) {
            throw new IllegalArgumentException("Impossibile creare il file '" + filename + "'.");
        }

        long fileSize = dis.readLong();

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));


        long currentBytes = 0L;

        while( currentBytes<fileSize ) {

            int availableBytes = 0;
            if( (availableBytes = dis.available())>0 ) {

                byte[] fileBytes = new byte[availableBytes];
                currentBytes += dis.read(fileBytes, 0, availableBytes);
                bos.write(fileBytes, 0, fileBytes.length);
                bos.flush();
            }

        }

        System.out.println("File " + filename
                + " downloaded (" + currentBytes + " bytes read)");

        bos.close();

        return "received/" + filename;


    }

    private void runCommandAndPrintOutput(String command) throws IOException {

        Process flashProcess = Runtime.getRuntime().exec(
                "cmd.exe /c " +
                        command
        );


        try {
            flashProcess.waitFor();

            int cnt=0;
            try {
                while ((cnt = flashProcess.getInputStream().available()) > 0) {
                    byte[] buffer = new byte[cnt];
                    flashProcess.getInputStream().read(buffer, 0, cnt);
                    System.out.println(new String(buffer));
                }
                System.out.println();
            } catch (Exception ignored) {}

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}