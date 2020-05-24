package it.unina.sistemiembedded.client;

import it.unina.sistemiembedded.model.Board;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Optional;

@Getter @Setter
public abstract class Client implements ClientCommandsInterface {

    protected static int BLOCKING_REQUEST_SECONDS_TIMEOUT = 20;

    /**
     * Client name
     */
    protected String name;

    /**
     * Server proxy
     */
    protected ServerProxy server;

    /**
     * Create a client with a custom name
     * @param name String name
     */
    protected Client(String name) {
        this.name = name.trim();
    }

    /**
     * Set blocking requests timeout time in seconds
     * @param timeout int timeout in seconds
     */
    public static void setBlockingTimeout(int timeout) {
        if(timeout>0)
            BLOCKING_REQUEST_SECONDS_TIMEOUT = timeout;
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
     * Get server name
     * @return String server name
     */
    public abstract String getServerName();

}
