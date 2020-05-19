package it.unina.sistemiembedded.server.impl;

import it.unina.sistemiembedded.exception.BoardAlreadyInUseException;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.ClientHandler;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.Commands;
import it.unina.sistemiembedded.utility.Constants;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.shared.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.Socket;
import java.util.List;

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
            System.out.println(ClientHandlerImpl.class+"%[stop] Client handler ( " + this.id + " ) has been stopped");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("[stop] There was an error while closing client handler (" + this.id +" socket");
            System.out.println(ClientHandlerImpl.class+"%[stop] There was an error while closing client handler (" + this.id + ") socket");
        }

        server.removeClientHandler(this);

    }

    @Override
    public void run() {

        if(socket.isClosed()) return;

        this.running = socket.isConnected();

        logger.info("[run] Client handler (" + this.id + ") has been started");
        System.out.println(ClientHandlerImpl.class+"%[run] Client handler ( " + this.id + " ) has been started");

        this.name = readMessageFromClient();
        logger.debug("[run] Client connected: (" + this.id + ", " + this.name + ")");
        System.out.println(ClientHandlerImpl.class+"%[run] Client connected: (" + this.id + ", " + this.name + ")");

        sendMessageToClient(this.server.getName());

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
        synchronized (this.board) {
            this.board.setInUse(true);
        }

        sendMessagesToClient(Commands.AttachOnBoard.BEGIN_TRANSFER_BOARD, board.serialize());

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

        if(this.board!=null) {
            synchronized (this.board) {
                this.board.setInUse(false);
            }
        }

        return this.board;
    }

    public void sendMessagesToClient(String ... messages) {

        synchronized (dos) {
            for (String m : messages) {
                sendMessageToClient(m);
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
                Board board = attachOnBoardRequestCallback();
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
                detachBoard();

                break;

            //
            // DETACH FROM BOARD

            case Commands.DetachFromBoard.REQUEST:
                stringBuilder.append("Detach board request");
                detachBoardRequestCallback();

                break;

            //
            // FLASH

            case Commands.Flash.REQUEST:
                stringBuilder.append("Flash on board request");
                flashRequestCallback();

                break;

            //
            // DEBUG
            case Commands.Debug.REQUEST:
                stringBuilder.append("Debug on board request");
                debugRequestCallback();

                break;

            case Commands.Debug.REQUEST_END:
                stringBuilder.append("End debug on board request");
                debugEndRequestCallback();

                break;

            //
            // INFO.Board list

            case Commands.Info.BOARD_LIST_REQUEST:
                stringBuilder.append("Board list request");
                boardListRequestCallback();

                break;

            default:

                stringBuilder.append("Ricevuto: ").append(message);

                break;

        }

        logger.info("[parseReceivedMessage] " + stringBuilder.toString());
        System.out.println(ClientHandlerImpl.class+"%[parseReceivedMessage] " + stringBuilder.toString());

        return stringBuilder.toString();

    }

    private Board attachOnBoardRequestCallback() {


        String serialNumber = readMessageFromClient();

        try {
            server.attachBoardOnClient(this, serialNumber);
        } catch (BoardAlreadyInUseException e) {
            sendMessageToClient(Commands.AttachOnBoard.BOARD_BUSY);
        } catch (BoardNotFoundException e) {
            sendMessageToClient(Commands.AttachOnBoard.BOARD_NOT_FOUND);
        }


        return this.board;

    }

    private void detachBoardRequestCallback() {

        if(this.getBoard()!=null) {
            synchronized (this.getBoard()) {
                this.getBoard().setInUse(false);
            }
            this.setBoard(null);
        }

        sendMessageToClient(Commands.DetachFromBoard.SUCCESS);

    }

    private void flashRequestCallback() {

        if(this.board==null) {
            sendMessageToClient(Commands.Flash.ERROR);
            return;
        }

        try {
            this.receiveFile(".elf", Commands.Flash.SUCCESS);

            // TODO: Avvia flash e applicazione !

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[flashBoardRequestCallback] There was an error while receiving the file: " + e.getMessage());
            System.out.println(ClientHandlerImpl.class+"%[flashBoardRequestCallback] There was an error while receiving the file: " + e.getMessage());
            sendMessageToClient(Commands.Flash.ERROR);
        }

    }

    private void debugRequestCallback() {

        if(this.board==null) {
            sendMessageToClient(Commands.Debug.ERROR);
            return;
        }

        int debugPort = Integer.parseInt(this.readMessageFromClient());

        // TODO: Avvia debug ! Sync on board or something like this
        // In un nuovo thread the manda un END OF DEBUG se si chiude il processo

    }

    private void debugEndRequestCallback() {

        if(this.board!=null) {

            synchronized (this.board) {

                this.board.getDebuggingProcess().destroyForcibly();
                try {
                    this.board.getDebuggingProcess().waitFor();
                } catch (InterruptedException ignored) {
                }

                this.board.setDebuggingProcess(null);

                sendMessageToClient(Commands.Debug.FINISHED);

            }

        }

    }

    private String readMessageFromClient() {

        String message = "";

        synchronized (dis) {
            try {
                message = this.dis.readUTF();
            } catch (IOException e) {
                if(this.running) {
                    logger.error("[readMessageFromClient] Connection lost");
                    System.out.println(ClientHandlerImpl.class+"%[readMessageFromClient] Connection lost");
                }
                this.stop();
            }
        }

        return message;

    }

    private void sendMessageToClient(String message) {

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
    private String receiveFile(String fileExtension, String postMessage) throws IOException {

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
                System.out.println(ClientHandlerImpl.class+"%[receiveFile] File transfer successfully completed.");

            }

            logger.info("[receiveFile] File " + filename
                    + " downloaded (" + currentBytes + " bytes read)");

            bos.close();

            if(!StringUtils.isBlank(postMessage)) {
                sendMessageToClient(postMessage);
            }

            return "received/" + board.getSerialNumber() + "/" + filename;

        }


    }

    private void boardListRequestCallback() {

        List<Board> boardList = this.server.listBoards();

        String[] messages = new String[2+boardList.size()];
        messages[0] = Commands.Info.BEGIN_OF_BOARD_LIST;
        messages[1] = String.valueOf(boardList.size());
        for (int i = 0; i < boardList.size(); i++) {
            messages[i+2] = boardList.get(i).serialize();
        }

        sendMessagesToClient(messages);

    }

}
