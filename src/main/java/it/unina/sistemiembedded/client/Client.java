package it.unina.sistemiembedded.client;

import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.exception.ClientNotConnectedException;
import it.unina.sistemiembedded.model.Board;

import java.io.IOException;

public abstract class Client {

    /**
     * Client name
     */
    protected String name;

    /**
     * Client connected state
     */
    protected boolean connected = false;

    /**
     * Ip of the connected server.
     */
    protected String serverIp;

    /**
     * Port of the connected server
     */
    protected int serverPort;

    /**
     * Attached board
     */
    protected Board board;

    /**
     * Create a client with a custom name
     * @param name String name
     */
    protected Client(String name) {
        this.name = name.trim();
    }

    /**
     * Start a new client connection to a specific server board.
     * @param serverIp String ip of the server
     * @param serverPort int port on which the server is listening on
     * @throws IOException if an I/O error occurs when creating the socket
     */
    public abstract void connect(String serverIp, int serverPort) throws IOException;

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
    public abstract void attachOnBoard(String serialNumber) throws ClientNotConnectedException, BoardNotFoundException;

    /**
     * Get client connected state
     * @return boolean true if connected, false otherwise
     */
    public boolean isConnected() { return this.connected;}
}
