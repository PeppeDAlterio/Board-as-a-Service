package it.unina.sistemiembedded.client.impl;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.client.ServerProxy;
import it.unina.sistemiembedded.exception.BoardNotAvailableException;
import it.unina.sistemiembedded.exception.NotConnectedException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.Commands;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.shared.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Getter @Setter
public class ClientImpl extends Client {

    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private static class Message {

        private Object payload;

    }

    private final Logger logger = LoggerFactory.getLogger(ClientImpl.class);

    private ServerProxy server;
    private String serverIpAddress;
    private int serverPort;

    private String serverName = "";

    private Board board;

    private Thread listeningThread;

    private Semaphore blockingReceivingRequest = new Semaphore(1);
    private Semaphore blockingReceivingBufferReady = new Semaphore(0);
    private BlockingReceivingMethod blockingReceivingMethod = BlockingReceivingMethod.none;
    private final Message blockingReceivingBuffer = new Message();

    private enum BlockingReceivingMethod {
        none, listConnectedServerBoards
    }


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

            waitForMessagesAsync();

            logger.info("[connected] Proxy to: "  + serverIp+":"+serverPort + " successfully created" );

        } catch (IOException e){
            logger.error("[connect] there was a problem during the connection to: " + serverIp + ":" + serverPort);
            JOptionPane.showMessageDialog(null,"[connect] there was a problem during the connection to: " + serverIp + ":" + serverPort
            ,"Error",JOptionPane.ERROR_MESSAGE);
            throw e;
        }

    }

    @Override
    public void disconnect() {

        this.releaseBoard();
        this.board = null;

        this.server.disconnect();

        this.listeningThread.interrupt();

        logger.info("[disconnect] Client disconnected");

    }

    @Override
    public void requestBoard(String boardId) throws NotConnectedException {

        if(!this.isConnected()) {
            throw new NotConnectedException();
        }

        this.server.sendMessages(Commands.AttachOnBoard.REQUEST_BOARD, boardId);

    }

    @Override
    public void releaseBoard() {
        this.server.sendMessage(Commands.DetachFromBoard.REQUEST);
    }

    @Override
    public boolean isConnected() {
        return (this.server!=null && this.server.isConnected());
    }

    @Override
    public void flash(String file) throws BoardNotAvailableException, IOException {

        if(!board().isPresent()) {
            throw new BoardNotAvailableException();
        }

        logger.debug("[flash] Flash requested ...");

        //TODO: Board specific extension !!
        this.server.sendFile(Commands.Flash.REQUEST, "", file, ".elf");

    }

    @Override
    public void debug(int port) throws BoardNotAvailableException, IllegalArgumentException {

        if(!board().isPresent()) {
            throw new BoardNotAvailableException();
        }

        if(port<0 || port>65535)
            throw new IllegalArgumentException("The port parameter is outside the specified range");

        logger.debug("[debug] Debug requested on port " + port + " ...");

        this.server.sendMessages(Commands.Debug.REQUEST, String.valueOf(port));

    }

    @Override
    public Optional<Board> board() {
        return Optional.ofNullable(this.board);
    }

    @Override
    public void sendTextMessage(String message) {

        // Avoids commands
        if(message.startsWith("$--- ") && message.endsWith(" ---$")) {
            message += " ";
        }

        this.server.sendMessage(message);
    }

    @Override
    public List<Board> listConnectedServerBoards() throws NotConnectedException {

        List<Board> boards = Collections.emptyList();

        blockingReceivingRequest.acquireUninterruptibly();
        blockingReceivingMethod = BlockingReceivingMethod.listConnectedServerBoards;
        try {
            this.server.sendMessage(Commands.Info.BOARD_LIST_REQUEST);
        } catch (Exception e) {
            blockingReceivingRequest.release();
            blockingReceivingMethod = BlockingReceivingMethod.none;
        }

        try {
            blockingReceivingBufferReady.tryAcquire(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        if(blockingReceivingBuffer.getPayload() instanceof List) {

            final LinkedList<Board> tmp = new LinkedList<>();
            ((List<?>) blockingReceivingBuffer.getPayload()).forEach(e -> {
                tmp.add((Board) e);
            });

            boards = Collections.unmodifiableList(tmp);

        }

        blockingReceivingRequest.release();
        blockingReceivingMethod = BlockingReceivingMethod.none;

        return boards;

    }

    @Override
    public void listConnectedServerBoardsAsync() throws NotConnectedException {

        if(!isConnected()) throw new NotConnectedException();

        this.server.sendMessage(Commands.Info.BOARD_LIST_REQUEST);

    }

    /**
     * Thread listening for new messages
     */
    private void waitForMessagesAsync() {

        if(listeningThread!=null && listeningThread.isAlive()) {
            logger.info("[waitForMessagesAsync] Thread already started and running");
            return;
        }

        listeningThread = new Thread( () -> {

            while (server.isConnected()) {

                try {

                    parseReceivedMessage(this.server.receiveString());

                } catch (IOException e) {
                    break;
                }

            }

            logger.error("[waitForMessagesAsync] Server disconnected");

        });

        listeningThread.start();

    }

    /**
     * Received message parse
     * @param message String received message
     * @throws IOException error with some commands that need other messages to be read
     */
    private void parseReceivedMessage(String message) throws IOException {

        if(StringUtils.isBlank(message)) return;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[parseReceivedMessage] ");

        switch (message) {

            // ATTACH ON BOARD

            case Commands.AttachOnBoard.BEGIN_TRANSFER_BOARD:
                stringBuilder.append("Transfer board message received");
                receiveAndSetBoardCallback();
                break;

            //
            // DETACH FROM BOARD

            case Commands.DetachFromBoard.SUCCESS:
                stringBuilder.append("Detach from board ack received");
                detachBoardCallback();
                break;

            //
            // FLASH

            case Commands.Flash.SUCCESS:
                stringBuilder.append("Detach from board success ack received");
                flashCallback("success");
                break;

            case Commands.Flash.ERROR:
                stringBuilder.append("Detach from board error ack received");
                flashCallback("error");
                break;

            //
            // INFO.BOARD LIST

            case Commands.Info.BEGIN_OF_BOARD_LIST:
                receiveBoardListCallback();
                break;

            //
            // SIMPLE MESSAGE

            default:
                stringBuilder.append("Received: ").append(message);
                break;

        }

        logger.debug(stringBuilder.toString());

    }

    /**
     * Receives a serialized board and set it.
     * @throws IOException error while receiving the board
     */
    private void receiveAndSetBoardCallback() throws IOException {

        String serializedBoard = this.server.receiveString();

        try {

            Board board = Board.deserialize(serializedBoard);
            board.setInUse(true);
            setBoard(board);

            this.server.sendMessage(Commands.AttachOnBoard.SUCCESS);

        } catch (Exception e) {
            logger.error("[parseReceivedMessage] Bad Board received: " + serializedBoard);
            this.server.sendMessage(Commands.AttachOnBoard.ERROR);
        }

    }

    /**
     * Concrete detaches the board
     */
    private void detachBoardCallback() {
        this.board = null;
    }

    /**
     *
     */
    private void flashCallback(String result) {

        logger.info("[flashCallback] Flash complete with result: " + result);

    }

    private void receiveBoardListCallback() {

        List<Board> boards = null;

        try {

            int numberOfBoards = Integer.parseInt(this.server.receiveString());

            boards = new ArrayList<>(numberOfBoards);

            for(int i=0; i<numberOfBoards; i++) {

                String serializedBoard = this.server.receiveString();
                boards.add(Board.deserialize(serializedBoard));

            }


        } catch (IOException e) {
            logger.error("[receiveBoardListCallback] There was an error while receiving board list");

            if(boards==null) {
                boards = Collections.emptyList();
            }

        } finally {

            if(blockingReceivingMethod!=BlockingReceivingMethod.none) {
                blockingReceivingBuffer.setPayload(boards);
                blockingReceivingBufferReady.release();
            }

        }

    }

}
