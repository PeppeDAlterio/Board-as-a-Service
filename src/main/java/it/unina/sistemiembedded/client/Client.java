package it.unina.sistemiembedded.client;

import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.exception.ClientNotConnectedException;

import java.io.IOException;

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
     */
    public abstract void connect(String serverIp, int serverPort) throws IOException;

    /**
     * Start a new client connection to a specific server.
     * Default Server port will be used (see Server.DEFUALT_PORT)
     * @param serverIp String ip of the server
     * @throws IOException if an I/O error occurs when creating the socket
     */
    public abstract void connect(String serverIp) throws IOException;

    /**
     * Stop the client connection, if established.
     */
    public abstract void disconnect();

    /**
     * Attache the client to a board offered by the connected server.
     * @param serialNumber String serial number of the requested board
     * @throws ClientNotConnectedException client is not connected to a server
     * @throws BoardNotFoundException board not found, or not available, in connected server
     */
    public abstract void attachOnBoardRequest(String serialNumber) throws ClientNotConnectedException, BoardNotFoundException;

    /**
     * Get client connected state
     * @return boolean true if connected, false otherwise
     */
    public abstract boolean isConnected();
}
