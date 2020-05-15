package it.unina.sistemiembedded.server;

import it.unina.sistemiembedded.exception.server.BoardAlreadyExistsException;
import it.unina.sistemiembedded.model.Board;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class Server {

    private final int DEFAULT_PORT = 1234;

    /**
     * Server name
     */
    protected String name;

    /**
     * Server listening port
     */
    protected int port = DEFAULT_PORT;

    /**
     * Server running state
     */
    protected boolean running = false;

    /**
     * Server boards map: (serial number, board)
     */
    protected Map<String, Board> boards = new HashMap<>();

    /**
     * Create a new Server with a name listening on the default port
     * @param name String name
     */
    protected Server(String name) {
        this.name = name;
    }

    /**
     * Create a new Server with a name listening on a given port
     * @param name String name
     * @param port int server custom port, between 0 and 65535, inclusive
     * @throws IllegalArgumentException if the port parameter is outside the specified range of valid port values,
     * which is between 0 and 65535, inclusive.
     */
    protected Server(String name, int port) {

        if(port<0 || port>65535) throw new IllegalArgumentException("The port parameter is outside the specified range");

        this.name = name.trim();

        this.port = port;
    }

    /**
     * Start the server.
     * @throws IOException if an I/O error occurs when creating the socket
     */
    abstract public void start() throws IOException;

    /**
     * Stop the server, if running
     */
    abstract public void stop();

    /**
     * Add a board to server boards
     * @param board Board new board to add
     */
    abstract public void addBoard(@Nonnull Board board) throws BoardAlreadyExistsException;

    /**
     * Get the server running state.
     * @return boolean true if running, false otherwise
     */
    public boolean isRunning() {return this.running;}

}
