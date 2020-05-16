package it.unina.sistemiembedded.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public abstract class ClientHandler implements Runnable {

    /**
     * Client handler id
     */
    protected long id;

    /**
     * Client name
     */
    protected String name;

    /**
     * Input stream of the client
     */
    protected final DataInputStream dis;

    /**
     * Output stream of the client
     */
    protected final DataOutputStream dos;

    /**
     * Socket connected to the client
     */
    protected final Socket socket;

    /**
     * Server that handles the client
     */
    protected final Server server;

    /**
     * Create a new Client Handler
     * @param id long client id
     * @param server Server Server that handles the client
     * @param socket Socket Socket connected to the client
     * @param dis DataInputStream Input stream of the client
     * @param dos DataOutputStream Output stream of the client
     */
    protected ClientHandler(long id, Server server, Socket socket, DataInputStream dis, DataOutputStream dos) {
        this.id = id;
        this.dis = dis;
        this.dos = dos;
        this.socket = socket;
        this.server = server;
    }

    /**
     * Stop the client.
     */
    public abstract void stop();

    /**
     * Get client handler status
     * @return boolean true if connected, false otherwise
     */
    public boolean isAlive() {return socket.isConnected();}

    /**
     * Get client handler ID
     * @return long client handler id
     */
    public long getId() {return this.id;}

}
