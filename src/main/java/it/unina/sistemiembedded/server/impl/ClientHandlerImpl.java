package it.unina.sistemiembedded.server.impl;

import it.unina.sistemiembedded.exception.BoardAlreadyInUseException;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.ClientHandler;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.Commands;
import lombok.Getter;
import org.apache.maven.shared.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@Getter
public class ClientHandlerImpl extends ClientHandler {

    private final Logger logger = LoggerFactory.getLogger(ClientHandlerImpl.class);

    /**
     * Client name
     */
    private String name;

    /**
     * Attached board. Null if not attached
     */
    private Board board;

    /**
     * Client running state. Please not that just the desiderata state, socket may be open/closed.
     * The isAlive() method concretely checks handler's alive status.
     */
    private boolean running;

    /**
     * Create a new Client Handler
     *
     * @param id     long client id
     * @param server Server Server that handles the client
     * @param socket Socket Socket connected to the client
     * @param dis    DataInputStream Input stream of the client
     * @param dos    DataOutputStream Output stream of the client
     */
    protected ClientHandlerImpl(long id, Server server, Socket socket, DataInputStream dis, DataOutputStream dos) {
        super(id, server, socket, dis, dos);
    }

    @Override
    public void stop() {

        this.running = false;

        try {
            if(this.socket.isConnected()) {
                this.socket.close();
            }
            logger.info("[stop] Client handler (" + this.id + ") has been stopped");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("[stop] There was an error while closing client handler (" + this.id +" socket");
        }

        server.removeClientHandler(this);

    }

    @Override
    public void run() {

        if(socket.isClosed()) return;

        this.running = socket.isConnected();

        logger.info("[run] Client handler (" + this.id + ") has been started");

        try {
            this.name = readMessageFromClient();
            logger.debug("[run] Client connected: (" + this.id + ", " + this.name + ")");
        } catch (IOException e) {
            e.printStackTrace();
            this.stop();
            return;
        }

        while(isAlive()) {
            try
            {
                // receive and parse a string message ...
                parseReceivedMessage(readMessageFromClient());

            } catch (IOException e) {
                if(this.running) {
                    logger.error("[run] Connection lost");
                }
                break;
            }

        }

        this.stop();

    }

    @Override
    public boolean isAlive() {
        return this.running && socket.isConnected();
    }

    @Override
    public Board attachBoard(@Nonnull Board board) {

        if(this.board == board) return this.board;

        detachBoard();
        this.board = board;
        this.board.setInUse(true);

        sendMessagesToClient(Commands.AttachOnBoard.Request.TRANSFER_BOARD, board.toString());

        return this.board;

    }

    @Override
    public Board detachBoard(Board board) {
        if(this.board == board) {
            return detachBoard();
        }
        return null;
    }

    @Override
    public Board detachBoard() {

        Board board = this.board;

        if(board!=null) {
            board.setInUse(false);
        }

        return board;
    }

    private String parseReceivedMessage(String message) {

        if(StringUtils.isBlank(message)) return "";

        logger.debug("[parseReceivedMessage] Beggining parse of message: " + message);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[ Client (").append(this.id).append(", ").append(this.name).append(") ] ");

        switch (message) {

            case Commands.AttachOnBoard.Request.REQUEST:

                stringBuilder.append(" Wants to attach on a board");
                Board board = startAttachOnBoard();

                break;

            case Commands.AttachOnBoard.Response.SUCCESS:
                stringBuilder.append(" Successfully attached on Board: ").append(this.board.getSerialNumber());

                break;

            case Commands.AttachOnBoard.Response.ERROR:
                stringBuilder.append(" There was an error attaching on board: ").append(this.board.getSerialNumber());
                detachBoard();

                break;

            default:

                stringBuilder.append("Ricevuto: ").append(message);

                break;

        }

        logger.info("[parseReceivedMessage] " + stringBuilder.toString());

        return stringBuilder.toString();

    }

    private Board startAttachOnBoard() {

        try {

            String serialNumber = readMessageFromClient();

            try {
                server.attachBoardOnClient(this, serialNumber);
            } catch (BoardAlreadyInUseException e) {
                sendMessageToClient(Commands.AttachOnBoard.Response.BOARD_BUSY);
            } catch (BoardNotFoundException e) {
                sendMessageToClient(Commands.AttachOnBoard.Response.BOARD_NOT_FOUND);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return this.board;

    }

    private String readMessageFromClient() throws IOException {
        return this.dis.readUTF();
    }

    public void sendMessagesToClient(String ... messages) {

        synchronized (dos) {
            for (String m : messages) {
                sendMessageToClient(m);
            }
        }


    }

    private void sendMessageToClient(String message) {

        try {
            synchronized (dos) {
                logger.debug("[sendMessageToClient] Sending message: '" + message + "' to: " + this.id);
                this.dos.writeUTF(message);
            }
        } catch (IOException e) {
            logger.error("[sendMessageToClient] Connectino lost");
            stop();
        }
    }

}
