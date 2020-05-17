package it.unina.sistemiembedded.server;

import it.unina.sistemiembedded.model.Board;

import java.net.Socket;

public abstract class ClientHandler implements Runnable {

    /**
     * Client handler id
     */
    protected long id;

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
     */
    protected ClientHandler(long id, Server server, Socket socket) {
        this.id = id;
        this.socket = socket;
        this.server = server;
    }

    /**
     * Stop the client.
     */
    public abstract void stop();

    /**
     * Get client handler status
     * @return boolean true if alive, false otherwise
     */
    public abstract boolean isAlive();

    public abstract Board attachBoard(Board board);

    public abstract Board detachBoard(Board board);

    public abstract Board detachBoard();

    /**
     * Get client handler ID
     * @return long client handler id
     */
    public long getId() {return this.id;}

}
