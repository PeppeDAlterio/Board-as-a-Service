package it.unina.sistemiembedded.client;

import it.unina.sistemiembedded.exception.BoardNotAvailableException;
import it.unina.sistemiembedded.exception.NotConnectedException;
import it.unina.sistemiembedded.model.Board;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Getter @Setter
public abstract class Client {

    /**
     * Client name
     */
    protected String name;

    /**
     * Create a client with a custom name
     * @param name String name
     */
    protected Client(String name) {
        this.name = name.trim();
    }

    /**
     * Start a new client connection to a specific server.
     * @param serverIp String ip of the server
     * @param serverPort int port on which the server is listening on
     * @throws IOException if an I/O error occurs when creating the socket
     * @throws IllegalArgumentException if the port parameter is outside the specified range of valid port values,
     *                                  which is between 0 and 65535, inclusive.
     */
    public abstract void connect(String serverIp, int serverPort) throws IOException, IllegalArgumentException;

    /**
     * Start a new client connection to a specific server.
     * Default Server port will be used (see Server.DEFAULT_PORT)
     * @param serverIp String ip of the server
     * @throws IOException if an I/O error occurs when creating the socket
     */
    public abstract void connect(String serverIp) throws IOException;

    /**
     * Stop the client connection, if established.
     */
    public abstract void disconnect();

    /**
     * Request a board from the connected server, async.
     *
     * @param boardSerialNumber
     * @throws NotConnectedException client is not connected to a server
     */
    public abstract void requestBoard(String boardSerialNumber) throws NotConnectedException;

    /**
     * Release the connected board, if exists.
     */
    public abstract void releaseBoard();

    /**
     * Request a flash on the connected board, async.
     * @param file String path to the file to flash
     * @throws BoardNotAvailableException no board connected
     */
    public abstract void requestFlash(String file) throws BoardNotAvailableException, IOException;

    /**
     * Request a flash on the connected board, async.
     * @param port int port to listen of for debug session
     * @throws BoardNotAvailableException no board connected
     */
    public abstract void requestDebug(int port) throws BoardNotAvailableException, IllegalArgumentException;

    /**
     * Get client connected state
     * @return boolean true if connected, false otherwise
     */
    public abstract boolean isConnected();

    /**
     * Get connected board, if exists. Empty otherwise
     * @return Optional connected board, if exists or empty otherwise
     */
    public abstract Optional<Board> board();

    /**
     * Sends a text message to the server, async
     * @param message String message to be sent
     */
    public abstract void sendTextMessage(String message);

    /**
     * Request connected server's board list, async
     * @throws NotConnectedException client is not connected to any server
     */
    public abstract void listConnectedServerBoardsAsync() throws NotConnectedException;

    /**
     * Blocking requests connected server's board list.
     * @throws NotConnectedException client is not connected to any server
     * @return List list or server's boards
     */
    public abstract List<Board> listConnectedServerBoards() throws NotConnectedException;

    /**
     * Get server name
     * @return String server name
     */
    public abstract String getServerName();

}
