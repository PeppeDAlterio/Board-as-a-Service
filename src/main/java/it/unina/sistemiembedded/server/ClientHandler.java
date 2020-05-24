package it.unina.sistemiembedded.server;

import it.unina.sistemiembedded.model.Board;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.Socket;
import java.util.UUID;

public abstract class ClientHandler implements Runnable {

    /**
     * Client handler id
     */
    protected final String id = UUID.randomUUID().toString();

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
     * @param server Server Server that handles the client
     * @param socket Socket Socket connected to the client
     */
    protected ClientHandler(Server server, Socket socket) {
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

    /**
     * Attach board on client handler
     * @param board Board board to attach client on
     * @return Board attached board, null if error
     */
    public abstract @Nullable Board attachBoard(@Nonnull Board board);

    /**
     * Detach board from client handler
     * @return Board detached board, null if error
     */
    public abstract @Nullable Board detachBoard();

    /**
     * Sends text message to client
     * @param message String text message to send
     */
    public abstract void sendTextMessage(String message);

    /**
     * Get client handler ID
     * @return long client handler id
     */
    public String getId() {return this.id;}

    public abstract String getName();

}
