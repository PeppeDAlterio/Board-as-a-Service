package it.unina.sistemiembedded.server;

import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.net.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.UUID;

public abstract class ClientHandler implements Runnable {

    /**
     * Client handler id
     */
    protected String id = UUID.randomUUID().toString();

    /**
     * Client name
     */
    protected String name;

    /**
     * Board used by the client
     */
    protected Board board;

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
     * @param dis DataInputStream Input stream of the client
     * @param dos DataOutputStream Output stream of the client
     * @param socket Socket Socket connected to the client
     * @param server Server Server that handles the client
     */
    protected ClientHandler(DataInputStream dis, DataOutputStream dos, Socket socket, Server server) {
        this.dis = dis;
        this.dos = dos;
        this.socket = socket;
        this.server = server;
    }

    /**
     * Get client handler status
     * @return boolean true if connected, false otherwise
     */
    public boolean isAlive() {return socket.isConnected();}

}
