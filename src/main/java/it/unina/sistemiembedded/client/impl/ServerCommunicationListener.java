package it.unina.sistemiembedded.client.impl;

import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.utility.communication.Commands;
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
 * Deletegator communication listener
 */
public class ServerCommunicationListener {

    private final Logger logger = LoggerFactory.getLogger(ServerCommunicationListener.class);

    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private static class Message {

        private Object payload;

    }

    private final ClientImpl client;

    private Semaphore blockingReceivingRequest = new Semaphore(1);
    private Semaphore blockingReceivingBufferReady = new Semaphore(0);
    private BlockingReceivingMethod blockingReceivingMethod = BlockingReceivingMethod.none;
    private final Message blockingReceivingBuffer = new Message();

    private enum BlockingReceivingMethod {
        none, listConnectedServerBoards
    }

    ServerCommunicationListener(ClientImpl client) {
        this.client = client;
    }

    /*
     *  BEGIN OF ASYNC CALLBACKS
     */

    void receiveAndSetBoardCallback() throws IOException {

        String serializedBoard = this.client.getServer().receiveString();

        try {

            Board board = Board.deserialize(serializedBoard);
            board.setInUse(true);
            this.client.setBoard(board);

            this.client.getServer().sendMessage(Commands.AttachOnBoard.SUCCESS);

        } catch (Exception e) {
            logger.error("[parseReceivedMessage] Bad Board received: " + serializedBoard);
            this.client.getServer().sendMessage(Commands.AttachOnBoard.ERROR);
        }

    }

    void detachBoardCallback() {
        this.client.setBoard(null);
    }

    void flashCallback(String result) {

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
        }
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

    /*
     *  END OF ASYNC CALLBACKS
     */

    /*
     * BEGIN OF BLOCKING REQUESTS
     */

    List<Board> blockingReceiveServerBoardList() {

        List<Board> boards = Collections.emptyList();

        requestBlockingReceiving(BlockingReceivingMethod.listConnectedServerBoards);
        try {
            this.client.getServer().sendMessage(Commands.Info.BOARD_LIST_REQUEST);
        } catch (Exception e) {
            releaseBlockingReceiving(BlockingReceivingMethod.listConnectedServerBoards);
            return Collections.emptyList();
        }

        try {

            blockingReceivingBufferReady.tryAcquire(20, TimeUnit.SECONDS);

            if(blockingReceivingBuffer.getPayload() instanceof List) {

                final LinkedList<Board> tmp = new LinkedList<>();
                ((List<?>) blockingReceivingBuffer.getPayload()).forEach(e -> {
                    tmp.add((Board) e);
                });

                boards = Collections.unmodifiableList(tmp);

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        releaseBlockingReceiving(BlockingReceivingMethod.listConnectedServerBoards);

        return boards;

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
        }

    }

}
