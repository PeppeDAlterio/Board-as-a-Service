package it.unina.sistemiembedded.client;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.Socket;

public abstract class ServerProxy {

    /**
     * Server socket
     */
    protected final Socket socket;

    /**
     * Server proxy initialization
     * @param socket Socket server socket
     * @throws IllegalArgumentException socket not connected
     * @throws IOException I/O error occurs when creating the input stream, the socket is closed,
     *                     the socket is not connected, or the socket input has been shutdown using
     */
    protected ServerProxy(@Nonnull Socket socket) {
        if(!socket.isConnected() || socket.isClosed()) throw new IllegalArgumentException("Socket is not connected");

        this.socket = socket;
    }

    /**
     * Blocking receive a message
     * @return String received message
     * @throws IOException connection lost or something wen wrong reading the message
     */
    public abstract String receive() throws IOException;

    /**
     * Transfers a file to the server
     * @param preMessage String message to be send before the file transfer
     * @param postMessage String message to be send when file transfer completes
     * @param file String path to the file to transfer
     * @param extension String file extension
     * @throws IOException
     */
    public abstract void sendFile(String preMessage, String postMessage, String file, String extension) throws IOException;

    /**
     * Send a string message to the server
     * @param msg String message
     */
    public abstract void sendMessage(String msg);

    /**
     * Use this method to atomic send multiple messages
     * @param messages String messages to send
     */
    public abstract void sendMessages(String ... messages);

    /**
     * Get server connected state
     * @return boolean true if connected, false otherwise
     */
    public abstract boolean isConnected();

    /**
     * Disconnect from the server
     */
    public abstract void disconnect();

}
