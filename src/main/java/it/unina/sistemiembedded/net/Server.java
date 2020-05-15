package it.unina.sistemiembedded.net;


import it.unina.sistemiembedded.main.MainServerApplicationGUI;
import it.unina.sistemiembedded.net.file.SocketFileHelper;
import it.unina.sistemiembedded.utility.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Server {

    public static class ClientHandler implements Runnable {

        private long id;
        private String name = "";
        private String board= "";

        final DataInputStream dis;
        final DataOutputStream dos;
        private final Socket socket;

        private final Server server;
        private MainServerApplicationGUI serverApplicationGUI;

        // constructor
        public ClientHandler(Socket socket, long clientId,
                             DataInputStream dis, DataOutputStream dos, Server server , MainServerApplicationGUI serverApplicationGUI) {
            this.id = clientId;
            this.dis = dis;
            this.dos = dos;
            this.socket = socket;
            this.server = server;
            this.serverApplicationGUI = serverApplicationGUI;
        }

        public long getId() { return this.id; }

        public String getName() { return this.name; }

        public String getBoard() {return this.board;}

        public DataOutputStream getDataOutputStream() { return this.dos; }

        public DataInputStream getDataInputStream() { return this.dis; }

        public void stop() {
            try {
                this.socket.close();
            } catch (IOException ignored) {}
        }

        @Override
        public void run() {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            try {
                this.name = dis.readUTF();
                this.board = dis.readUTF();
                server.clients.put(getId(), this);
                System.out.println("\t[ "+formatter.format(date)+" ] New client connected [ID : " + this.id + " | NOME : " + this.name + " | BOARD : " + this.board + " ]\t\n");
                serverApplicationGUI.setStoryClientConnection("\t[ "+formatter.format(date)+" ] New client connected [ ID : " + this.id + " | NOME : " + this.name + " | BOARD : " + this.board + " ]\t\n");
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

                    if(buffer.equals(Constants.END_OF_REMOTE_FLASH)) {
                        System.out.println("\t[ Client (" + this.id + ", " + this.name + ") ] Flash remoto completato.");
                    } else if (buffer.equals(Constants.BEGIN_OF_DEBUG)) {
                        System.out.println("\t[ Client (" + this.id + ", " + this.name + ") ] Sessione di debug remoto avviata." +
                                " Utilizza l'ambiente di sviluppo verso questo IP: " + this.socket.getInetAddress()
                                + " con il porto specificato in precedenza.");
                    } else if(buffer.equals(Constants.END_OF_DEBUG)) {
                        System.out.println("\t[ Client (" + this.id + ", " + this.name + ") ] Sessione di debug remoto terminata.");
                    } else {

                        System.out.println("\t[ Client (" + this.id + ", " + this.name + ") ] Ricevuto: " + buffer);

                    }

                } catch (IOException e) {
                    this.server.removeClient(this);
                    break;
                }

            }

            this.server.removeClient(this);

        }

        @Override
        public String toString() {
            return "ID: " + this. id + " | NOME: " + this.name + " | BOARD: "+this.board;
        }

    }

    // Server main thread
    private Thread serverThread;

    // Threads handlng clients
    private final List<Thread> clientThreads = new LinkedList<>();
    // ArrayList to store active client handlers
    private final Map<Long, ClientHandler> clients = new HashMap<>();
    // Sequencer for client unique ID
    private final AtomicLong sequencer = new AtomicLong(0);

    // Server socket
    private ServerSocket serverSocket;
    // Server socket port
    private final int port;
    private boolean running = false;
    private MainServerApplicationGUI serverApplicationGUI;

    public Server(int port , MainServerApplicationGUI serverApplicationGUI) {
        this.port = port;
        this.serverApplicationGUI = serverApplicationGUI;
    }

    public void startServer() throws IOException {

        synchronized (this) {

            if(running) return;

            serverSocket = new ServerSocket(port);

            System.out.println("Server avviato. In attesa di connessioni...");

            this.running = true;

        }

        serverThread = new Thread( () -> {

            try {

                while(this.running) {

                    Socket socket = serverSocket.accept();

                    //System.out.println("\n\nNew client connection request received : " + socket);

                    // obtain input and output streams
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                    // Create a new handler object for handling this request.
                    ClientHandler clientHandler = new ClientHandler(socket, sequencer.getAndIncrement(),
                            dis, dos, this,serverApplicationGUI);

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
        this.running = false;
    }

    public boolean isRunning() {return this.running;}

    public String getClientNameById(long id) {

        if(this.clients.get(id) != null) {
            return this.clients.get(id).getName();
        } else {
            return null;
        }

    }

    public void removeClient(ClientHandler clientHandler) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        System.out.println("\t[ "+formatter.format(date)+" ] client [ ID : " + clientHandler.getId() +
                " | NOME : " + clientHandler.getName() + " | BOARD : " + clientHandler.getBoard() + " ] disconnected \t\n");
        serverApplicationGUI.setStoryClientConnection("\t[ "+formatter.format(date)+" ] client [ ID : " + clientHandler.getId() +
                " | NOME : " + clientHandler.getName() + " | BOARD : " + clientHandler.getBoard() + " ] disconnected \t\n");
        this.clients.remove(clientHandler.getId(), clientHandler);
    }

    public List<ClientHandler> getClients() { return new ArrayList<>(this.clients.values()); }

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

        SocketFileHelper.sendFile(clientHandler.getDataOutputStream(), file);

    }

    private Optional<ClientHandler> getClientById(long id) {
        return Optional.ofNullable(this.clients.get(id));
    }

}
