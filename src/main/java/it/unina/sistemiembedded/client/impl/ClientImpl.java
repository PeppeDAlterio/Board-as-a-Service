package it.unina.sistemiembedded.client.impl;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.exception.BoardAlreadyInUseException;
import it.unina.sistemiembedded.exception.BoardNotAvailableException;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.exception.NotConnectedException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.communication.Commands;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.shared.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter @Setter
public class ClientImpl extends Client {

    private final Logger logger = LoggerFactory.getLogger(ClientImpl.class);

    private String serverName = "";
    private String serverIpAddress;
    private int serverPort;

    private Board board;

    private ExecutorService listeningExecutor = Executors.newSingleThreadExecutor();

    private ServerCommunicationListener serverCommunicationListener;


    public ClientImpl(String name) {
        super(name);
    }

    @Override
    public void connect(String serverIp) throws IOException {
        connect(serverIp, Server.DEFAULT_PORT);
    }

    @Override
    public void connect(String serverIp, int serverPort) throws IOException {

        if(serverPort<0 || serverPort>65535)
            throw new IllegalArgumentException("The port parameter is outside the specified range");

        try {

            this.server = new ServerProxyImpl(new Socket(InetAddress.getByName(serverIp), serverPort));

            this.serverIpAddress = serverIp;
            this.serverPort = serverPort;

            this.server.sendMessage(this.name);

            this.serverName = this.server.receiveString();

            serverCommunicationListener = new ServerCommunicationListener(this);

            waitForMessagesAsync();

            logger.info("[connected] Proxy to: "  + serverIp+":"+serverPort + " successfully created" );

        } catch (IOException e){
            logger.error("[connect] there was a problem during the connection to: " + serverIp + ":" + serverPort);
            throw e;
        }

    }

    @Override
    public void disconnect() {

        this.requestReleaseBoard();
        this.board = null;

        this.server.disconnect();

        this.listeningExecutor.shutdownNow();

        logger.info("[disconnect] Client disconnected");

        //TODO: Informa la UI

    }

    @Override
    public void requestBoard(String boardSerialNumber) throws NotConnectedException {

        checkConnection();

        this.server.sendMessages(Commands.AttachOnBoard.REQUEST_BOARD, boardSerialNumber);

    }

    @Override
    public void requestReleaseBoard() throws NotConnectedException {

        checkConnection();

        this.server.sendMessage(Commands.DetachFromBoard.REQUEST);
    }

    @Override
    public boolean isConnected() {
        return (this.server!=null && this.server.isConnected());
    }

    @Override
    public void requestFlash(String file) throws NotConnectedException, BoardNotAvailableException, IOException {

        checkConnection();

        if(board().isEmpty()) {
            throw new BoardNotAvailableException();
        }

        logger.debug("[flash] Flash requested ...");

        //TODO: Board specific extension !!
        this.server.sendFile(Commands.Flash.REQUEST, "", file, ".elf");

    }

    @Override
    public void requestDebug(int port) throws NotConnectedException, BoardNotAvailableException, IllegalArgumentException {

        checkConnection();

        if(board().isEmpty()) {
            throw new BoardNotAvailableException();
        }

        if(port<0 || port>65535)
            throw new IllegalArgumentException("The port parameter is outside the specified range");

        logger.debug("[debug] Debug requested on port " + port + " ...");

        this.server.sendMessages(Commands.Debug.REQUEST, String.valueOf(port));

    }

    @Override
    public void requestStopDebug() {
        this.server.sendMessage(Commands.Debug.REQUEST_END);
    }

    @Override
    public Optional<Board> board() {
        return Optional.ofNullable(this.board);
    }

    @Override
    public void sendTextMessage(String message) throws NotConnectedException {

        checkConnection();

        // Avoids commands
        if(message.startsWith("$--- ") && message.endsWith(" ---$")) {
            message += " ";
        }

        this.server.sendMessage(message);
    }

    @Override
    public void requestServerBoardList() throws NotConnectedException {

        checkConnection();

        this.server.sendMessage(Commands.Info.BOARD_LIST_REQUEST);

    }

    /*
     *  BEGIN OF BLOCKING REQUESTS
     */

    @Override
    public List<Board> requestBlockingServerBoardList() throws NotConnectedException {

        checkConnection();

        return serverCommunicationListener.blockingReceiveServerBoardList(BLOCKING_REQUEST_SECONDS_TIMEOUT);
    }

    @Override
    public Board requestBlockingBoard(String boardSerialNumber) throws NotConnectedException, BoardNotFoundException, BoardAlreadyInUseException {

        checkConnection();

        return serverCommunicationListener.blockingRequestBoard(boardSerialNumber, BLOCKING_REQUEST_SECONDS_TIMEOUT);
    }

    @Override
    public boolean requestBlockingFlash(String file) throws NotConnectedException, BoardNotAvailableException, IOException {

        checkConnection();

        return serverCommunicationListener.blockingFlash(file, BLOCKING_REQUEST_SECONDS_TIMEOUT);

    }

    /*
     *  END OF BLOCKING REQUESTS
     */

    private void checkConnection() {
        if (!isConnected()) {
            throw new NotConnectedException();
        }
    }

    /**
     * Thread listening for new messages
     */
    private void waitForMessagesAsync() {

        if (!listeningExecutor.isTerminated()) {
            listeningExecutor.shutdownNow();
            listeningExecutor = Executors.newSingleThreadExecutor();
        }

        listeningExecutor.execute( () -> {

            while (server.isConnected()) {

                try {

                    parseReceivedMessage(this.server.receiveString());

                } catch (IOException e) {
                    break;
                }

            }

            logger.error("[waitForMessagesAsync] Server disconnected");
            serverCommunicationListener.serverDisconnectedCallback();

        });

    }

    /**
     * Received message parse
     * @param message String received message
     * @throws IOException error with some commands that need other messages to be read
     */
    private void parseReceivedMessage(String message) throws IOException {

        if(StringUtils.isBlank(message)) return;

        String loggerPrefix = "[parseReceivedMessage] ";

        StringBuilder loggerStringBuilder = new StringBuilder();
        loggerStringBuilder.append(loggerPrefix);

        switch (message) {

            // ATTACH ON BOARD

            case Commands.AttachOnBoard.BEGIN_TRANSFER_BOARD:
                loggerStringBuilder.append("Transfer board message received");
                serverCommunicationListener.receiveBoardBeginTransferCallback();
                break;

            case Commands.AttachOnBoard.BOARD_BUSY:
                loggerStringBuilder.append("Requested board is already attached to another client");
                serverCommunicationListener.receiveBoardBusyCallback();
                break;

            case Commands.AttachOnBoard.BOARD_NOT_FOUND:
                loggerStringBuilder.append("Requested board was not found");
                serverCommunicationListener.receiveBoardNotFoundCallback();
                break;

            //
            // DETACH FROM BOARD

            case Commands.DetachFromBoard.SUCCESS:
                loggerStringBuilder.append("Detach from board ack received");
                serverCommunicationListener.detachBoardCallback();
                break;

            //
            // FLASH

            case Commands.Flash.SUCCESS:
                loggerStringBuilder.append("Detach from board success ack received");
                serverCommunicationListener.flashCallback(true);
                break;

            case Commands.Flash.ERROR:
                loggerStringBuilder.append("Detach from board error ack received");
                serverCommunicationListener.flashCallback(false);
                break;

            //
            // DEBUG

            case Commands.Debug.STARTED:
                loggerStringBuilder.append("Debugging session started");
                serverCommunicationListener.startedDebugCallback();
                break;

            case Commands.Debug.ERROR:
            case Commands.Debug.FINISHED:
                loggerStringBuilder.append("Debugging session finished");
                serverCommunicationListener.finishedDebugCallback();
                break;

            //
            // INFO.BOARD LIST

            case Commands.Info.BEGIN_OF_BOARD_LIST:
                serverCommunicationListener.receiveBoardListCallback();
                break;

            //
            // Interrupt.SERVER DISCONNECTED

            case Commands.Interrupt.SERVER_DISCONNECTED:
                serverCommunicationListener.serverDisconnectedCallback();
                break;

            //
            // SIMPLE MESSAGE

            default:
                loggerStringBuilder.append("Received: ").append(message);
                break;

        }

        if(!loggerStringBuilder.toString().equals("[parseReceivedMessage] "))
            logger.debug(loggerStringBuilder.toString());

    }

}
