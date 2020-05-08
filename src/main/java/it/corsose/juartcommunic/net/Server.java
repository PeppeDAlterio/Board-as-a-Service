package it.corsose.juartcommunic.net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Server {

    public static class ClientHandler implements Runnable {

        private long id;
        private String name = "";

        final DataInputStream dis;
        final DataOutputStream dos;
        private final Socket socket;

        private final Server server;


        // constructor
        public ClientHandler(Socket socket, long clientId,
                             DataInputStream dis, DataOutputStream dos, Server server) {
            this.id = clientId;
            this.dis = dis;
            this.dos = dos;
            this.socket = socket;
            this.server = server;
        }

        public long getId() { return this.id; }

        public String getName() { return this.name; }

        public DataOutputStream getDataOutputStream() { return this.dos; }

        public DataInputStream getDataInputStream() { return this.dis; }

        public void stop() {
            try {
                this.socket.close();
            } catch (IOException ignored) {}
        }

        @Override
        public void run() {

            //System.out.println("In attesa del nome per il client con ID: " + this.id + " ...");
            try {
                this.name = dis.readUTF();
                server.clients.put(getId(), this);
                System.out.println("\t<<<Nuovo client connesso con id:: " + this.id + " e nome " + this.name + ">>>");
            } catch (IOException e) {
                //e.printStackTrace();
                this.server.removeClient(this);
                return;
            }

            String buffer;
            while (true) {

                try
                {
                    // receive the string
                    buffer = dis.readUTF();
                    if(buffer.equalsIgnoreCase("exit")) break;

                    System.out.println("\t[ Client ("+this.id+", "+this.name+") ] Ricevuto: " + buffer);

                } catch (IOException e) {
                    this.server.removeClient(this);
                    return;
                }

            }

            this.server.removeClient(this);

        }

        @Override
        public String toString() {
            return "Id: " + this. id + ", nome: " + this.name;
        }

    }

    private ServerSocket serverSocket;

    private Thread serverThread;
    private List<Thread> clientThreads = new LinkedList<>();

    // ArrayList to store active clients
    private Map<Long, ClientHandler> clients = new HashMap<>();
    private int port;

    private boolean started = false;

    private AtomicLong sequencer = new AtomicLong(0);

    public Server(int port) {
        this.port = port;
    }

    public void startServer() throws IOException {

        synchronized (this) {

            if(started) return;

            serverSocket = new ServerSocket(port);

            System.out.println("Server avviato. In attesa di connessioni...");

            this.started = true;

        }

        serverThread = new Thread( () -> {

            try {

                while(true) {

                    Socket socket = serverSocket.accept();

                    //System.out.println("\n\nNew client connection request received : " + socket);

                    // obtain input and output streams
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                    // Create a new handler object for handling this request.
                    ClientHandler clientHandler = new ClientHandler(socket, sequencer.getAndIncrement(),
                            dis, dos, this);

                    // Create a new Thread with this object.
                    Thread thread = new Thread(clientHandler);

                    clientThreads.add(thread);
                    thread.start();

                }

            } catch (IOException e) {
                System.err.println("Server disconnesso.");
            }


        });

        serverThread.start();

    }

    public void stopServer() {
        try {
            serverSocket.close();
        } catch (IOException ignored) {}
        clients.values().forEach(ClientHandler::stop);
        clientThreads.forEach(Thread::interrupt);
        serverThread.interrupt();
    }

    public void removeClient(ClientHandler clientHandler) {
        System.out.println("\t[ Client ("+clientHandler.getId()+", "+clientHandler.getName()+") ] Disconnesso.");
        this.clients.remove(clientHandler.getId(), clientHandler);
    }

    public List<ClientHandler> getClients() {
        return new ArrayList<>(this.clients.values());
    }

    public void sendMessage(long id, String message) {

        ClientHandler clientHandler = this.clients.get(id);
        if(clientHandler != null) {

            try {

                clientHandler.getDataOutputStream().writeUTF(message);

                //System.out.println("Inviato '" + message + "' a " + id);
            } catch (IOException e) {
                //e.printStackTrace();
            }

        } else {
            throw new IllegalArgumentException();
        }

    }

    public void sendFile(long clientId, String file) throws IOException {

        ClientHandler clientHandler = getClientById(clientId).orElseThrow(IllegalArgumentException::new);

        File myFile = new File(file);

        if(!myFile.exists()) {
            throw new IllegalArgumentException("Il file specificato non esiste");
        }

        FileInputStream fis = new FileInputStream(myFile);

        clientHandler.dos.writeUTF("--- BEGIN OF FILE TX ---");

        clientHandler.dos.writeUTF(myFile.getName());

        clientHandler.dos.writeLong(myFile.length());

        long totalCount = 0L;
        int count;
        byte[] buffer = new byte[1024];
        while ( (count = fis.read(buffer)) > 0) {
            clientHandler.dos.write(buffer, 0, count);
            clientHandler.dos.flush();
            totalCount += count;
        }

        System.out.println("Trasferito: " + totalCount);

        System.out.println("...trasferimento completato.");

        fis.close();

    }

    private Optional<ClientHandler> getClientById(long id) {
        return Optional.ofNullable(this.clients.get(id));
    }

    public String getClientNameById(long id) {

        if(this.clients.get(id) != null) {
            return this.clients.get(id).getName();
        } else {
            return null;
        }

    }

}
