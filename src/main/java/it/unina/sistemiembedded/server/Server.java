package it.unina.sistemiembedded.server;

import it.unina.sistemiembedded.driver.COMPort;
import it.unina.sistemiembedded.exception.AlreadyConnectedException;
import it.unina.sistemiembedded.exception.BoardAlreadyExistsException;
import it.unina.sistemiembedded.exception.BoardAlreadyInUseException;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.model.Board;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Getter
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
     * @param boardSerialNumber String board serial number
     * @throws BoardNotFoundException board not found on the server
     * @return Server this
     */
    abstract public Server removeBoard(String boardSerialNumber) throws BoardNotFoundException;

    /**
     * Remove a board from the server board list
     * @param serialNumbers String[] list of board serial numbers
     * @throws BoardNotFoundException board not found on the server
     * @return Server this
     */
    abstract public Server removeBoards(String ... serialNumbers) throws BoardNotFoundException;

    /**
     * Get the server running state.
     * @return boolean true if running, false otherwise
     */
    abstract public boolean isRunning();

    /**
     * Get a copy of all boards shared by the server
     * @return List list copy of Boards
     */
    abstract public List<Board> listBoards();

    abstract public boolean existsBoardBySerialNumber(String serialNumber);

    abstract public boolean existsBoardById(String boardId);

    abstract public @Nullable Board attachBoardOnClient(ClientHandler clientHandler, String boardSerialNumber)
            throws BoardNotFoundException, BoardAlreadyInUseException;

    /**
     * Removes a client handler for the server
     * @param clientHandler ClientHandler client handler to be removed
     */
    abstract public void removeClientHandler(@Nonnull ClientHandler clientHandler);

    /**
     * Set server name, if the server is not already running.
     * If the server is running AlreadyConnectedException will be thrown
     * @param name String server name
     * @throws AlreadyConnectedException if the server is already running
     */
    public void setName(String name) throws AlreadyConnectedException {

        if(isRunning()) {
            throw new AlreadyConnectedException();
        }

        this.name = name;

    }

    /**
     * Reduils board list from available attached boards
     * @return List list of available attached boards
     */
    public abstract List<Board> rebuildBoards();

    /**
     * Set COM port to a board
     * @param boardSerialNumber String board serial number
     * @param comPort COMPort com port
     * @param baudRate int baud rate
     * @param numBitData int number of data bits
     * @param bitStop int bit stop
     * @param parity String parity
     * @param flowControl Collection flow control strings
     * @throws BoardNotFoundException if board not found by serial number
     */
    public abstract void setBoardCOMDriver(@Nonnull String boardSerialNumber, @Nullable COMPort comPort,
                                           int baudRate, int numBitData, int bitStop, String parity, Collection<String> flowControl)
            throws BoardNotFoundException;

}