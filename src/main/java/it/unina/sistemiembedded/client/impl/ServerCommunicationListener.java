package it.unina.sistemiembedded.client.impl;

import it.unina.sistemiembedded.exception.BoardAlreadyInUseException;
import it.unina.sistemiembedded.exception.BoardNotAvailableException;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.utility.communication.Commands;
import it.unina.sistemiembedded.utility.ui.client.UIServerDisconnectedHelper;
import it.unina.sistemiembedded.utility.ui.stream.UIPrinterHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Deletegator communication listener.
 * Async callbacks called by the parser of the received message are listed here
 * as well as blocking version of requests
 */
public class ServerCommunicationListener {

    private final Logger logger = LoggerFactory.getLogger(ServerCommunicationListener.class);

    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private static class Message {

        private Object payload;

    }

    private final ClientImpl client;

    private final Semaphore blockingReceivingRequest = new Semaphore(1);
    private final Semaphore blockingReceivingBufferReady = new Semaphore(0);
    private BlockingReceivingMethod blockingReceivingMethod = BlockingReceivingMethod.none;
    private final Message blockingReceivingBuffer = new Message();

    private enum BlockingReceivingMethod {
        none, listConnectedServerBoards, requestBoard, flash, reset
    }

    ServerCommunicationListener(ClientImpl client) {
        this.client = client;
    }

    void serverDisconnectedCallback() {
        this.client.disconnect();
        UIServerDisconnectedHelper.uiServerDisconnectedCallback();
    }

    /*
     *  BEGIN OF ASYNC CALLBACKS
     */

    void receiveBoardBusyCallback() {

        fillBlockingReceiving(BlockingReceivingMethod.requestBoard, Board.busyEmptyBoard());

        logger.debug("[receiveBoardBusyBoardCallback] Requested a busy board");

    }

    void receiveBoardNotFoundCallback() {

        fillBlockingReceiving(BlockingReceivingMethod.requestBoard, null);

        logger.debug("[receiveBoardNotFoundCallback] Requested a non-existing board");

    }

    void receiveBoardBeginTransferCallback() throws IOException {

        String serializedBoard = this.client.getServer().receiveString();

        try {

            Board board = Board.deserialize(serializedBoard);
            board.setInUse(true);
            this.client.setBoard(board);

            this.client.getServer().sendMessage(Commands.AttachOnBoard.SUCCESS);

            fillBlockingReceiving(BlockingReceivingMethod.requestBoard, board);

        } catch (Exception e) {
            logger.error("[parseReceivedMessage] Bad Board received: " + serializedBoard);
            this.client.getServer().sendMessage(Commands.AttachOnBoard.ERROR);
        }

    }

    void detachBoardCallback() {
        this.client.setBoard(null);
    }

    void flashCallback(boolean result) {

        fillBlockingReceiving(BlockingReceivingMethod.flash, result);

        logger.info("[flashCallback] Flash complete with result: " + result);

    }

    void finishedDebugCallback() {
        if(this.client.getBoard()!=null) {
            synchronized (this.client.getBoard().getSerialNumber().intern()) {
                this.client.getBoard().setDebugging(false);
            }
        }
    }

    void startedDebugCallback() {
        if(this.client.getBoard()!=null) {
            synchronized (this.client.getBoard().getSerialNumber().intern()) {
                this.client.getBoard().setDebugging(true);
            }
            UIPrinterHelper.clientDebug("Debugger is ready!");
            UIPrinterHelper.clientDebug("To correctly use the remote debbugger :");
            UIPrinterHelper.clientDebug("\t1)  Open your STM32CubeIDE");
            UIPrinterHelper.clientDebug("\t2)  Open 'Degub Configuration' settings ");
            UIPrinterHelper.clientDebug("\t3)  In the 'Debbugger' section enable 'Connect to remote GDB server");
            UIPrinterHelper.clientDebug("\t4)  Insert the server ip and the port specified above");
            UIPrinterHelper.clientDebug("\t5)  Click on 'Apply' and then 'Degub' buttons");
            UIPrinterHelper.clientDebug("\t6)  Start debbugging!\n ");
        }
    }

    void busyPortDebugCallback() {
        UIPrinterHelper.clientDebug("Debugging session couldn't start: the given port is busy");
    }

    void errorDebugCallback() {
        UIPrinterHelper.clientDebug("Debugging session error");
    }

    void receiveBoardListCallback() {

        List<Board> boards = null;

        try {

            int numberOfBoards = Integer.parseInt(this.client.getServer().receiveString());

            boards = new ArrayList<>(numberOfBoards);

            for(int i=0; i<numberOfBoards; i++) {

                String serializedBoard = this.client.getServer().receiveString();
                boards.add(Board.deserialize(serializedBoard));

            }


        } catch (IOException e) {
            logger.error("[receiveBoardListCallback] There was an error while receiving board list");

            if(boards==null) {
                boards = Collections.emptyList();
            }

        } finally {

            fillBlockingReceiving(BlockingReceivingMethod.listConnectedServerBoards, boards);

        }

    }

    void genericMessageReceivedCallback(String message) {
        UIPrinterHelper.clientMessage(message);
    }

    void successResetCallback() {

        logger.info("[successResetCallback] Board reset success");
        //JOptionPane.showMessageDialog(null,"Board reset successful","Reset success",JOptionPane.INFORMATION_MESSAGE);

        fillBlockingReceiving(BlockingReceivingMethod.reset, true);

    }

    void errorResetCallback() {

        logger.error("[successResetCallback] Board reset error");
        //JOptionPane.showMessageDialog(null,"Board reset error!","Reset error",JOptionPane.INFORMATION_MESSAGE);

        fillBlockingReceiving(BlockingReceivingMethod.reset, false);

    }

    /*
     *  END OF ASYNC CALLBACKS
     */

    /*
     * BEGIN OF BLOCKING REQUESTS
     */

    boolean blockingBoardReset(int timeout) throws BoardNotAvailableException {

        boolean result;

        requestBlockingReceiving(BlockingReceivingMethod.reset);
        try {
            this.client.requestReset();
        } catch (Exception e) {
            releaseBlockingReceiving(BlockingReceivingMethod.reset);
            throw e;
        }

        try {

            blockingReceivingBufferReady.tryAcquire(timeout, TimeUnit.SECONDS);

            if(blockingReceivingBuffer.getPayload() instanceof Boolean) {

                result = (Boolean) blockingReceivingBuffer.getPayload();

            } else {
                result = false;
            }

        } catch (InterruptedException e) {
            result = false;
        } finally {
            releaseBlockingReceiving(BlockingReceivingMethod.reset);
        }

        return result;

    }

    /**
     * Blocking receives server's board list
     * @param timeout int timeout in seconds
     * @return List of server's board or empty in case of errors
     */
    List<Board> blockingReceiveServerBoardList(int timeout) {

        List<Board> boards = Collections.emptyList();

        requestBlockingReceiving(BlockingReceivingMethod.listConnectedServerBoards);
        try {
            this.client.requestServerBoardList();
        } catch (Exception e) {
            releaseBlockingReceiving(BlockingReceivingMethod.listConnectedServerBoards);
            return Collections.emptyList();
        }

        try {

            blockingReceivingBufferReady.tryAcquire(timeout, TimeUnit.SECONDS);

            if(blockingReceivingBuffer.getPayload() instanceof List) {

                final LinkedList<Board> tmp = new LinkedList<>();
                ((List<?>) blockingReceivingBuffer.getPayload()).forEach(e -> {
                    tmp.add((Board) e);
                });

                boards = Collections.unmodifiableList(tmp);

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            releaseBlockingReceiving(BlockingReceivingMethod.listConnectedServerBoards);
        }

        return boards;

    }

    Board blockingRequestBoard(String boardSerialNumber, int timeout) throws BoardNotFoundException, BoardAlreadyInUseException {

        Board receivedBoard;

        requestBlockingReceiving(BlockingReceivingMethod.requestBoard);
        try {
            this.client.requestBoard(boardSerialNumber);
        } catch (Exception e) {
            releaseBlockingReceiving(BlockingReceivingMethod.requestBoard);
            throw e;
        }

        try {

            blockingReceivingBufferReady.tryAcquire(timeout, TimeUnit.SECONDS);

            if(blockingReceivingBuffer.getPayload() instanceof Board) {

                receivedBoard = (Board) blockingReceivingBuffer.getPayload();

                if(!receivedBoard.equals(this.client.getBoard()) && receivedBoard.isInUse()) {
                    throw new BoardAlreadyInUseException();
                }

            } else {
                throw new BoardNotFoundException();
            }

        } catch (InterruptedException e) {
            throw new BoardNotFoundException();
        } finally {
            releaseBlockingReceiving(BlockingReceivingMethod.requestBoard);
        }

        return receivedBoard;

    }

    boolean blockingFlash(String file, int timeout) throws IOException {

        Boolean result = false;

        requestBlockingReceiving(BlockingReceivingMethod.flash);
        try {
            this.client.requestFlash(file);
        } catch (Exception e) {
            releaseBlockingReceiving(BlockingReceivingMethod.flash);
            throw e;
        }

        try {

            blockingReceivingBufferReady.tryAcquire(timeout, TimeUnit.SECONDS);

            if(blockingReceivingBuffer.getPayload() instanceof Boolean) {
                result = (Boolean) blockingReceivingBuffer.getPayload();
            }

        } catch (InterruptedException e) {
            throw new IOException();
        } finally {
            releaseBlockingReceiving(BlockingReceivingMethod.flash);
        }

        return result;

    }

    /*
     * END OF BLOCKING REQUESTS
     */

    /**
     * Fill blocking receiving for given method, if any, with requested payload
     * @param requestMethod BlockingReceivingMethod method that requested the data
     * @param payload Object requested data
     */
    private void fillBlockingReceiving(BlockingReceivingMethod requestMethod, Object payload) {
        if(blockingReceivingMethod==requestMethod) {
            blockingReceivingBuffer.setPayload(payload);
            blockingReceivingBufferReady.release();
        }
    }

    /**
     * Requests blocking receiving
     * @param method BlockingReceivingMethod method that requests the blocking receive
     */
    private void requestBlockingReceiving(BlockingReceivingMethod method) {

        blockingReceivingRequest.acquireUninterruptibly();
        blockingReceivingMethod = method;

    }

    /**
     * Release the blocking receiving lock requested by the method, if any
     * @param method BlockingReceivingMethod method that requested the blocking receive
     */
    private void releaseBlockingReceiving(BlockingReceivingMethod method) {

        if(blockingReceivingMethod == method) {
            blockingReceivingRequest.release();
            blockingReceivingMethod = BlockingReceivingMethod.none;
            blockingReceivingBuffer.setPayload(null);
        }

    }

}
