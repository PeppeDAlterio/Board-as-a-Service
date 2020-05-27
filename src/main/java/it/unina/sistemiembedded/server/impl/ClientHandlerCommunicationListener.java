package it.unina.sistemiembedded.server.impl;

import it.unina.sistemiembedded.exception.BoardAlreadyInUseException;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.utility.SystemHelper;
import it.unina.sistemiembedded.utility.communication.Commands;
import it.unina.sistemiembedded.utility.debug.GDBProcess;
import it.unina.sistemiembedded.utility.ui.stream.UIPrinterHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Delegator listener.
 * Async callbacks called by the parser of the received message are listed here
 */
public class ClientHandlerCommunicationListener {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandlerCommunicationListener.class);

    private final ClientHandlerImpl clientHandler;

    ClientHandlerCommunicationListener(ClientHandlerImpl clientHandler) {
        this.clientHandler = clientHandler;
    }

    Board attachOnBoardRequestCallback() {

        String boardSerialNumber = this.clientHandler.readMessageFromClient();

        try {
            this.clientHandler.getServer().attachBoardOnClient(this.clientHandler, boardSerialNumber);
        } catch (BoardAlreadyInUseException e) {
            this.clientHandler.sendTextMessage(Commands.AttachOnBoard.BOARD_BUSY);
        } catch (BoardNotFoundException e) {
            this.clientHandler.sendTextMessage(Commands.AttachOnBoard.BOARD_NOT_FOUND);
        }


        return this.clientHandler.getBoard();

    }

    void attachOnBoardErrorCallback() {
        this.clientHandler.detachBoard();
    }

    void detachBoardRequestCallback() {

        if(this.clientHandler.getBoard()!=null) {
            synchronized (this.clientHandler.getBoard()) {
                this.clientHandler.getBoard().setInUse(false);
            }
            this.clientHandler.setBoard(null);
        }

        this.clientHandler.sendTextMessage(Commands.DetachFromBoard.SUCCESS);

    }

    void flashRequestCallback() {

        if(this.clientHandler.getBoard()==null) {
            this.clientHandler.sendTextMessage(Commands.Flash.ERROR);
            return;
        }

        stopActiveDebugSession();

        try {
            String receivedFile = this.clientHandler.receiveFile(".elf", null);

            SystemHelper.flash(this.clientHandler.getBoard().getSerialNumber(), receivedFile, this.clientHandler);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[flashBoardRequestCallback] There was an error while receiving the file: " + e.getMessage());
            UIPrinterHelper.serverActionPrint("There was an error while receiving the file: " + e.getMessage());
            this.clientHandler.sendTextMessage(Commands.Flash.ERROR);
        }

    }

    void debugRequestCallback() {

        if(this.clientHandler.getBoard()==null) {
            this.clientHandler.sendTextMessage(Commands.Debug.ERROR);
            return;
        }

        stopActiveDebugSession();

        int debugPort = Integer.parseInt(this.clientHandler.readMessageFromClient());

        try {
            this.clientHandler.getBoard().setDebugging(true);
            Process osProcess = SystemHelper.remoteDebug(this.clientHandler.getBoard().getSerialNumber(), debugPort, this.clientHandler);
            GDBProcess gdbProcess = new GDBProcess(osProcess, this.clientHandler.getIpAddress(), debugPort);
            this.clientHandler.getBoard().setDebuggingProcess(gdbProcess);
        } catch (Exception e) {
            this.clientHandler.sendTextMessages(Commands.Debug.ERROR);
            stopActiveDebugSession();
            logger.error("[debugRequestCallbak] There was an error while starting remote debug session on port: " + debugPort);
        }

    }

    void boardListRequestCallback() {

        List<Board> boardList = this.clientHandler.getServer().listBoards();

        String[] messages = new String[2+boardList.size()];
        messages[0] = Commands.Info.BEGIN_OF_BOARD_LIST;
        messages[1] = String.valueOf(boardList.size());
        for (int i = 0; i < boardList.size(); i++) {
            messages[i+2] = boardList.get(i).serialize();
        }

        this.clientHandler.sendTextMessages(messages);

    }

    void debugEndRequestCallback() {

        if(this.clientHandler.getBoard()!=null) {

            synchronized (this.clientHandler.getBoard().getSerialNumber().intern()) {

                stopActiveDebugSession();

                this.clientHandler.sendTextMessage(Commands.Debug.FINISHED);

            }

        }

    }

    void boardResetRequestCallback() {

        if(this.clientHandler.getBoard()!=null) {

            synchronized (this.clientHandler.getBoard().getSerialNumber().intern()) {
                SystemHelper.reset(this.clientHandler.getBoard().getSerialNumber(), this.clientHandler);
            }

        } else {
            this.clientHandler.sendTextMessage(Commands.Reset.ERROR);
        }

    }

    /**
     * Utility to stop active debug session, if any
     */
    void stopActiveDebugSession() {
        if(this.clientHandler.getBoard()!=null) {
            synchronized (this.clientHandler.getBoard().getSerialNumber().intern()) {
                this.clientHandler.getBoard().setDebugging(false);
                if (this.clientHandler.getBoard().getDebuggingProcess() != null) {
                    try {
                        Process process = this.clientHandler.getBoard().getDebuggingProcess().stopDebug();
                        if(process!=null) {
                            process.waitFor();
                        }
                    } catch (InterruptedException ignored) {
                    } finally {
                        this.clientHandler.getBoard().setDebuggingProcess(null);
                    }
                }
            }
        }
    }


}
