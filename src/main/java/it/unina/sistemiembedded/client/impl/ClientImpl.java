package it.unina.sistemiembedded.client.impl;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.exception.ClientNotConnectedException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.Commands;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter @Setter
public class ClientImpl extends Client {

    private final Logger logger = LoggerFactory.getLogger(ClientImpl.class);

    private String serverIpAddress;
    private int serverPort;

    private Socket socket;

    private Thread listeningThread;

    private DataInputStream dis;
    private DataOutputStream dos;

    /**
     * Lock for DataOutputStream
     */
    private final Lock messagingLock = new ReentrantLock(true);

    private Board connectedBoard;

    //private Process debugProcess = null;

    public ClientImpl(String name) {
        super(name);
    }

    @Override
    public void connect(String serverIp) throws IOException {
        connect(serverIp, Server.DEFAULT_PORT);
    }

    @Override
    public void connect(String serverIp, int serverPort) throws IOException {

        try {

            this.socket = new Socket(InetAddress.getByName(serverIp), serverPort);

            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());

            sendMessage(this.name);

            this.serverIpAddress = serverIp;
            this.serverPort = serverPort;

            waitForMessagesAsync();

            logger.info("[connected] Connected to: "  + serverIp+":"+serverPort );

        } catch (IOException e){
            logger.error("[connect] there was a problem during the connection to: " + serverIp + ":" + serverPort);
            throw e;
        }

    }

    @Override
    public void disconnect() {

        try {
            this.socket.close();
        } catch (IOException ignored) {}

        this.listeningThread.interrupt();

        logger.info("[disconnect] Client disconnected");

    }

    @Override
    public void attachOnBoardRequest(String serialNumber) throws ClientNotConnectedException, BoardNotFoundException {

        sendMessages(Commands.AttachOnBoard.Request.REQUEST, serialNumber);

    }

    @Override
    public boolean isConnected() {
        return this.socket.isConnected();
    }

    private void waitForMessagesAsync() {

        listeningThread = new Thread( () -> {

            String buffer;
            while (isConnected()) {

                try {
                    parseReceivedMessage(this.dis.readUTF());
                } catch (IOException e) {
                    logger.error("[waitForMessagesAsync] Connection lost");
                    break;
                }

            }

        });

        listeningThread.start();

    }

    private void parseReceivedMessage(String message) throws IOException {

        switch (message) {

            case Commands.AttachOnBoard.Request.TRANSFER_BOARD:
                logger.debug("[parseReceivedMessage] Transfer board message received");
                attachOnBoard();
                break;

            default:
                logger.info("[parseReceivedMessage] Received: " + message);
                break;
        }

    }

    private void attachOnBoard() throws IOException {
        String serializedBoard = this.dis.readUTF();
        String[] boardData = serializedBoard.split(Board.SERIALIZATION_SEPARATOR);
        if(boardData.length!=Board.SERIALIZATION_NUMBER_OF_FIELDS) {
            logger.error("[parseReceivedMessage] Bad Board received: " + serializedBoard);
            sendMessage(Commands.AttachOnBoard.Response.ERROR);
        } else {
            setConnectedBoard(new Board(boardData[0], boardData[1], null));
            sendMessage(Commands.AttachOnBoard.Response.SUCCESS);
        }
    }

    private void setConnectedBoard(Board connectedBoard) {
        this.connectedBoard = connectedBoard;
        this.connectedBoard.setInUse(true);
    }

    public void sendMessages(String ... messages) {

        messagingLock.lock();
        try {
            for (String m : messages) {
                sendMessage(m);
            }
        } finally {
            messagingLock.unlock();
        }


    }

    /**
     * The method is NOT thread-safe !!
     * @param msg String message to send over DataOutputStream
     */
    public void sendMessage(String msg) {

        if(!isConnected()) return;

        messagingLock.lock();
        try {
            logger.debug("[sendMessage] Sending message: " + msg);
            this.dos.writeUTF(msg);
        } catch (IOException e) {
            logger.error("[sendMessage] Connection lost.");
            this.disconnect();
        } finally {
            messagingLock.unlock();
        }

    }

}
