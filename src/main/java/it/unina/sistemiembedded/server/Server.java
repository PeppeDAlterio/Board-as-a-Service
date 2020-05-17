package it.unina.sistemiembedded.server;

import it.unina.sistemiembedded.exception.BoardAlreadyExistsException;
import it.unina.sistemiembedded.exception.BoardAlreadyInUseException;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.model.Board;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public abstract class Server {

    public static final int DEFAULT_PORT = 1234;

    /**
     * Server name
     */
    protected String name;

    /**
     * Server listening port
     */
    protected int port = DEFAULT_PORT;

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
     * @throws IOException if an I/O error occurs when closing the socket
     */
    abstract public void stop() throws IOException;

    /**
     * Add a board to server boards
     * @param board Board new board to add
     * @return Server this
     */
    abstract public Server addBoard(@Nonnull Board board) throws BoardAlreadyExistsException;

    /**
     * Add multple boards to server boards
     * @param boards Board new board to add
     * @return Server this
     */
    abstract public Server addBoards(@Nonnull Board ... boards) throws BoardAlreadyExistsException;

    /**
     * Remove a board from the server board list
     * @param boardId String board object id
     * @throws BoardNotFoundException board not found on the server
     * @return Server this
     */
    abstract public Server removeBoard(String boardId) throws BoardNotFoundException;

    /**
     * Get the server running state.
     * @return boolean true if running, false otherwise
     */
    abstract public boolean isRunning();

    /**
     * Get all boards shared by the server
     * @return List list of Boards
     */
    abstract public List<Board> listBoards();

    abstract public boolean existsBoardBySerialNumber(String serialNumber);

    abstract public boolean existsBoardById(String boardId);

    abstract public @Nullable Board attachBoardOnClient(ClientHandler clientHandler, String boardId)
            throws BoardNotFoundException, BoardAlreadyInUseException;

    /**
     * Removes a client handler for the server
     * @param clientHandler ClientHandler client handler to be removed
     */
    abstract public void removeClientHandler(@Nonnull ClientHandler clientHandler);

}