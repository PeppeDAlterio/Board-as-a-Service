package it.unina.sistemiembedded.server.impl;

import it.unina.sistemiembedded.driver.COMDriver;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.ClientHandler;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.communication.Commands;
import it.unina.sistemiembedded.utility.ui.stream.UIPrinterHelper;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

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
     * If true means that's already been stopped
     */
    private AtomicBoolean stopped = new AtomicBoolean(false);

    /**
     * Server connected time timestamp
     */
    private Date connectedTimestamp;

    /**
     * Create a new Client Handler
     *
     * @param server Server Server that handles the client
     * @param socket Socket Socket connected to the client
     */
    protected ClientHandlerImpl(Server server, Socket socket) throws IOException {
        super(server, socket);

        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());

        this.communicationListener = new ClientHandlerCommunicationListener(this);

    }

    @Override
    public void stop() {

        if(!stopped.compareAndSet(false, true)) return;

        if (this.running) {
            logger.info("[stop] Client handler (" + this.id + ") has been stopped");
            UIPrinterHelper.serverActionPrint("Client '" + this.name + "' disconnected");
        }

        this.communicationListener.stopActiveDebugSession();

        this.running = false;

        this.detachBoard();

        try {
            if(this.socket.isConnected()) {
                this.sendTextMessage(Commands.Interrupt.SERVER_DISCONNECTED);
                this.socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("[stop] There was an error while closing client handler (" + this.id +" socket");
        }

        server.removeClientHandler(this);

    }

    @Override
    public void run() {

        if(socket.isClosed()) return;

        this.stopped.set(false);

        this.running = socket.isConnected();

        // FIXME: check this out
        assert this.running;

        this.connectedTimestamp = new Date();

        logger.info("[run] Client handler (" + this.id + ") has been started");

        this.name = readMessageFromClient();
        logger.debug("[run] Client connected: (" + this.id + ", " + this.name + ")");
        UIPrinterHelper.serverActionPrint("New client connected. Hello '" + this.name + "'");

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
            this.board.getComDriver().ifPresent(comDriver -> comDriver.setClientHandler(this));
        }

        sendTextMessages(Commands.AttachOnBoard.BEGIN_TRANSFER_BOARD, board.serialize());

        return this.board;

    }

    @Override
    public Board detachBoard() {

        if(this.board!=null) {
            synchronized (this.board.getSerialNumber().intern()) {
                this.board.setInUse(false);
                this.board.getComDriver().ifPresent(COMDriver::removeClientHandler);
            }
        }

        return this.board;
    }

    @Override
    public String getIpAddress() {

        if(isAlive()) {
            return this.socket.getInetAddress().toString().replace("/", "");
        } else {
            return "";
        }

    }

    @Override
    public Optional<Board> getConnectedBoard() {
        return Optional.ofNullable(this.board);
    }

    public void sendTextMessages(String ... messages) {

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
        stringBuilder.append("[ Client (").append(this.name).append(") ] ");

        switch (message) {

            // ATTACH ON BOARD

            case Commands.AttachOnBoard.REQUEST_BOARD:

                Board board = communicationListener.attachOnBoardRequestCallback();
                if(board!=null) {
                    stringBuilder.append(" request a board: ");
                    stringBuilder.append(this.board.toString());
                } else {
                    stringBuilder.append(" board busy or not found");
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
                UIPrinterHelper.serverCommunicationPrint("[ "+formatter.format(date)+" ]   "+message);
                this.board.getComDriver().ifPresent(comDriver -> {
                    comDriver.writeln(message);
                });
                return stringBuilder.toString();

        }


        logger.info("[parseReceivedMessage] " + stringBuilder.toString());
        UIPrinterHelper.serverActionPrint(stringBuilder.toString());

        return stringBuilder.toString();

    }

    String readMessageFromClient() {

        String message = "";

        synchronized (dis) {
            try {
                message = this.dis.readUTF();
            } catch (IOException e) {
                this.stop();
            }
        }

        return message;

    }

    @Override
    public void sendTextMessage(String message) {

        try {
            synchronized (dos) {
                logger.debug("[sendTextMessage] Sending message: '" + message + "' to: " + this.id);
                this.dos.writeUTF(message);
            }
        } catch (IOException e) {
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

            if (!beginOfTx.equals(Commands.FileTransfer.BEGIN_FILE_TX)) {
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

            logger.info("[receiveFile] File " + filename
                    + " downloaded (" + currentBytes + " bytes read)");

            bos.close();

            if (endOfTx.equals(Commands.FileTransfer.END_OF_FILE_TX)) {
                logger.debug("[receiveFile] File transfer successfully completed.");
                UIPrinterHelper.serverActionPrint("File received from '"
                        + this.socket.getInetAddress() + "/" + this.name + "/" + this.board.getSerialNumber() + " : "
                        + filename + " (" + currentBytes + " bytes)");
            }

            if(!StringUtils.isBlank(postMessage)) {
                sendTextMessage(postMessage);
            }

            return "received/" + board.getSerialNumber() + "/" + filename;

        }


    }

    Server getServer() {return this.server;}

}
