package it.unina.sistemiembedded.server.impl;

import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.ClientHandler;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.Constants;
import it.unina.sistemiembedded.utility.RedirectStream;
import it.unina.sistemiembedded.utility.communication.Commands;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.shared.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter @Setter
public class ClientHandlerImpl extends ClientHandler {

    private final Logger logger = LoggerFactory.getLogger(ClientHandlerImpl.class);

    /**
     * Input stream of the client
     */
    private final DataInputStream dis;

    /**
     * Output stream of the client
     */
    private final DataOutputStream dos;

    private final ClientHandlerCommunicationListener communicationListener;

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
     */
    protected ClientHandlerImpl(long id, Server server, Socket socket) throws IOException {
        super(id, server, socket);

        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());

        this.communicationListener = new ClientHandlerCommunicationListener(this);

    }

    @Override
    public void stop() {

        this.running = false;

        this.detachBoard();

        try {
            if(this.socket.isConnected()) {
                this.socket.close();
            }
            logger.info("[stop] Client handler (" + this.id + ") has been stopped");
            System.out.println(RedirectStream.TEXT_AREA_ACTION_SERVER +"Client handler ( " + this.id + " ) has been stopped");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("[stop] There was an error while closing client handler (" + this.id +" socket");
            System.out.println(RedirectStream.TEXT_AREA_ACTION_SERVER+"There was an error while closing client handler (" + this.id + ") socket");
        }

        server.removeClientHandler(this);

    }

    @Override
    public void run() {

        if(socket.isClosed()) return;

        this.running = socket.isConnected();

        logger.info("[run] Client handler (" + this.id + ") has been started");
        System.out.println(RedirectStream.TEXT_AREA_ACTION_SERVER+"[run] Client handler ( " + this.id + " ) has been started");

        this.name = readMessageFromClient();
        logger.debug("[run] Client connected: (" + this.id + ", " + this.name + ")");
        System.out.println(RedirectStream.TEXT_AREA_ACTION_SERVER+"[run] Client connected: (" + this.id + ", " + this.name + ")");

        sendTextMessage(this.server.getName());

        while(isAlive()) {

            // receive and parse a string message ...
            parseReceivedMessage(readMessageFromClient());

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
        synchronized (this.board.getSerialNumber().intern()) {
            this.board.setInUse(true);
        }

        sendMessagesToClient(Commands.AttachOnBoard.BEGIN_TRANSFER_BOARD, board.serialize());

        return this.board;

    }

    @Override
    public Board detachBoard() {

        if(this.board!=null) {
            synchronized (this.board.getSerialNumber().intern()) {
                this.board.setInUse(false);
            }
        }

        return this.board;
    }

    public void sendMessagesToClient(String ... messages) {

        synchronized (dos) {
            for (String m : messages) {
                sendTextMessage(m);
            }
        }


    }

    private String parseReceivedMessage(String message) {

        if(StringUtils.isBlank(message)) return "";

        logger.debug("[parseReceivedMessage] Beggining parse of message: " + message);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[ Client (").append(this.id).append(", ").append(this.name).append(") ] ");

        switch (message) {

            // ATTACH ON BOARD

            case Commands.AttachOnBoard.REQUEST_BOARD:

                stringBuilder.append(" request a board: ");
                Board board = communicationListener.attachOnBoardRequestCallback();
                if(board!=null) {
                    stringBuilder.append(this.board.toString());
                } else {
                    stringBuilder.append("??");
                }

                break;

            case Commands.AttachOnBoard.SUCCESS:
                stringBuilder.append("Successfully attached on Board: ").append(this.board.toString());

                break;

            case Commands.AttachOnBoard.ERROR:
                stringBuilder.append("There was an error attaching on board: ").append(this.board.getSerialNumber());
                communicationListener.attachOnBoardErrorCallback();

                break;

            //
            // DETACH FROM BOARD

            case Commands.DetachFromBoard.REQUEST:
                stringBuilder.append("Detach board request");
                communicationListener.detachBoardRequestCallback();

                break;

            //
            // FLASH

            case Commands.Flash.REQUEST:
                stringBuilder.append("Flash on board request");
                communicationListener.flashRequestCallback();

                break;

            //
            // DEBUG
            case Commands.Debug.REQUEST:
                stringBuilder.append("Debug on board request");
                communicationListener.debugRequestCallback();

                break;

            case Commands.Debug.REQUEST_END:
                stringBuilder.append("End debug on board request");
                communicationListener.debugEndRequestCallback();

                break;

            //
            // INFO.Board list

            case Commands.Info.BOARD_LIST_REQUEST:
                stringBuilder.append("Board list request");
                communicationListener.boardListRequestCallback();

                break;

            default:

                stringBuilder.append("Received: ").append(message);
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                System.out.println(RedirectStream.TEXT_AREA_COMUNICATION_SERVER+"[ "+formatter.format(date)+" ]   "+message);

                break;

        }

        if(!stringBuilder.toString().contains("Received")) {
            logger.info("[parseReceivedMessage] " + stringBuilder.toString());
            System.out.println(RedirectStream.TEXT_AREA_ACTION_SERVER + stringBuilder.toString());
        }
        return stringBuilder.toString();

    }

    String readMessageFromClient() {

        String message = "";

        synchronized (dis) {
            try {
                message = this.dis.readUTF();
            } catch (IOException e) {
                if(this.running) {
                    logger.error("[readMessageFromClient] Connection lost");
                    System.out.println(RedirectStream.TEXT_AREA_ACTION_SERVER+"[readMessageFromClient] Connection lost");
                }
                this.stop();
            }
        }

        return message;

    }

    @Override
    public void sendTextMessage(String message) {

        try {
            synchronized (dos) {
                logger.debug("[sendMessageToClient] Sending message: '" + message + "' to: " + this.id);
                this.dos.writeUTF(message);
            }
        } catch (IOException e) {
            logger.error("[sendMessageToClient] Connectino lost");
            this.stop();
        }
    }

    /**
     * Receives a file from a DataInputStream
     * @param fileExtension String expected file extensions
     * @param postMessage String message to be send when file's been received
     * @return Received file path
     * @throws IOException error while receiving file
     */
    String receiveFile(String fileExtension, String postMessage) throws IOException {

        synchronized (dis) {

            String beginOfTx = dis.readUTF();

            if (!beginOfTx.equals(Constants.BEGIN_FILE_TX)) {
                throw new IllegalStateException("Begin of file transmission expected!");
            }

            String filename = dis.readUTF();

            if (!filename.trim().toLowerCase().endsWith(fileExtension.trim().toLowerCase())) {
                throw new IllegalStateException("File with extension '" + fileExtension + "' expected!");
            }

            filename = filename.trim();

            File directory = new File("received/" + board.getSerialNumber());
            if (!directory.exists() && !directory.mkdir()) {
                throw new IllegalArgumentException("Impossibile creare la cartella");
            }

            File newFile = new File("received/" + board.getSerialNumber() + "/" + filename);

            if (newFile.exists() && !newFile.delete()) {
                throw new IllegalArgumentException("Esiste già un file col nome '" + filename + "' e non è possibile cancellarlo.");
            }

            if (!newFile.createNewFile()) {
                throw new IllegalArgumentException("Impossibile creare il file '" + filename + "'.");
            }

            long fileSize = dis.readLong();

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));

            long currentBytes = 0L;

            while (currentBytes < fileSize) {

                int availableBytes;
                if ((availableBytes = dis.available()) > 0) {

                    int bytesToRead = Math.min((int) (fileSize - currentBytes), availableBytes);

                    byte[] fileBytes = new byte[bytesToRead];
                    currentBytes += dis.read(fileBytes, 0, bytesToRead);
                    bos.write(fileBytes, 0, bytesToRead);
                    bos.flush();

                }

            }

            String endOfTx = dis.readUTF();

            if (endOfTx.equals(Constants.END_FILE_TX)) {
                logger.debug("[receiveFile] File transfer successfully completed.");
                System.out.println("ClientAction%[receiveFile] File transfer successfully completed.");

            }

            logger.info("[receiveFile] File " + filename
                    + " downloaded (" + currentBytes + " bytes read)");

            bos.close();

            if(!StringUtils.isBlank(postMessage)) {
                sendTextMessage(postMessage);
            }

            return "received/" + board.getSerialNumber() + "/" + filename;

        }


    }

    Server getServer() {return this.server;}

}
