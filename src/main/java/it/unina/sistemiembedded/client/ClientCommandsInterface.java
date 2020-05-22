package it.unina.sistemiembedded.client;

import it.unina.sistemiembedded.exception.BoardAlreadyInUseException;
import it.unina.sistemiembedded.exception.BoardNotAvailableException;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.exception.NotConnectedException;
import it.unina.sistemiembedded.model.Board;

import java.io.IOException;
import java.util.List;

/**
 * Client interface for server communication
 */
public interface ClientCommandsInterface {

    /**
     * Sends a text message to the server, async
     * @param message String message to be sent
     * @throws NotConnectedException client is not connected to a server
     */
    void sendTextMessage(String message) throws NotConnectedException;

    /**
     * Request a flash on the connected board, async.
     * @param file String path to the file to flash
     * @throws NotConnectedException client is not connected to a server
     * @throws BoardNotAvailableException no board connected
     * @throws IOException if there's an error while communicating with the server
     */
    void requestFlash(String file) throws NotConnectedException, BoardNotAvailableException, IOException;

    /**
     * Request a flash on the connected board, async.
     * @param port int port to listen of for debug session
     * @throws NotConnectedException client is not connected to a server
     * @throws BoardNotAvailableException no board connected
     * @throws IllegalArgumentException invalid port range
     */
    void requestDebug(int port) throws NotConnectedException, BoardNotAvailableException, IllegalArgumentException;

    /**
     * Request end of debug process.
     */
    void requestStopDebug() throws NotConnectedException;

    /**
     * Request a board from the connected server, async.
     * @param boardSerialNumber String requested board serial number
     * @throws NotConnectedException client is not connected to a server
     */
    void requestBoard(String boardSerialNumber) throws NotConnectedException;

    /**
     * Release the connected board, if exists.
     * @throws NotConnectedException client is not connected to a server
     */
    void requestReleaseBoard() throws NotConnectedException;

    /**
     * Request connected server's board list, async
     * @throws NotConnectedException client is not connected to any server
     */
    void requestServerBoardList() throws NotConnectedException;

    /**
     * Blocking requests connected server's board list.
     * @throws NotConnectedException client is not connected to any server
     * @return List list or server's boards
     */
    List<Board> requestBlockingServerBoardList() throws NotConnectedException;

    /**
     * Request a board from the connected server, blocking
     * @param boardSerialNumber String board serial number
     * @return Optional requested board, empty if busy or not found
     * @throws NotConnectedException client is not connected to a server
     * @throws BoardNotAvailableException the board is attached to another client
     */
    Board requestBlockingBoard(String boardSerialNumber) throws NotConnectedException, BoardNotAvailableException, BoardNotFoundException, BoardAlreadyInUseException;

}
