package it.unina.sistemiembedded.server.impl;

import it.unina.sistemiembedded.driver.COMDriver;
import it.unina.sistemiembedded.driver.COMPort;
import it.unina.sistemiembedded.exception.BoardAlreadyExistsException;
import it.unina.sistemiembedded.exception.BoardAlreadyInUseException;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.ClientHandler;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.SystemHelper;
import it.unina.sistemiembedded.utility.ui.stream.UIPrinterHelper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Getter
public class ServerImpl extends Server {

    private final Logger logger = LoggerFactory.getLogger(ServerImpl.class);

    /**
     * Server running state
     */
    private boolean running = false;

    /**
     * Server boards map: (serialNumber, board)
     */
    private final Map<String, Board> boards = new HashMap<>();

    /**
     * Server client handlers map: (id, client handler)
     */
    private final Map<String, ClientHandler> clientHandlers = new HashMap<>();

    /**
     * RW lock for boards data structure
     */
    private final ReadWriteLock boardsRWLock = new ReentrantReadWriteLock();

    /**
     * RW lock for client handlers data structure
     */
    private final ReadWriteLock clientHandlersRWLock = new ReentrantReadWriteLock();

    private ServerSocket serverSocket;

    /**
     * The server main thread waiting for client connections
     */
    private Thread serverMainThread;

    public ServerImpl(String name) {
        super(name);
    }

    public ServerImpl(String name, int port) {
        super(name, port);
    }

    @Override
    public void start() throws IOException {

        if(isRunning()) {
            logger.warn("[start] Server already started");
            return;
        }

        synchronized (this) {

            serverSocket = new ServerSocket(port);

            logger.info("[start] Server successfully started");
            UIPrinterHelper.serverActionPrint("Welcome " + getName() + " :)");

            this.running = true;

        }

        waitForClientsAsync();

    }

    @Override
    public void stop() throws IOException {

        logger.info("[stop] Stopping Server ...");

        clientHandlersRWLock.writeLock().lock();
        try {

            List<ClientHandler> list = new LinkedList<>(this.clientHandlers.values());
            list.forEach(ClientHandler::stop);
            this.clientHandlers.clear();

        } finally {
            clientHandlersRWLock.writeLock().unlock();
        }


        try {
            if(this.serverSocket!=null && !this.serverSocket.isClosed()) {
                this.serverSocket.close();
            }
        } catch (Exception e) {
            logger.error("[stop] There was an error while closing server socket");
            throw e;
        }

        if(this.serverMainThread!=null && this.serverMainThread.isAlive()) {
            this.serverMainThread.interrupt();
            try {
                this.serverMainThread.join();
            } catch (InterruptedException ignored) {}
        }

        logger.info("[stop] ...Server successfully stopped.");

        this.running = false;

    }

    @Override
    public Server addBoard(@Nonnull Board board) throws BoardAlreadyExistsException {

        boardsRWLock.writeLock().lock();
        try {
            if (listBoards().contains(board) || boards.putIfAbsent(board.getSerialNumber(), board) != null) {
                throw new BoardAlreadyExistsException("Board '" + board.getSerialNumber() + "' already exists");
            }
        } finally {
            boardsRWLock.writeLock().unlock();
        }

        return this;

    }

    @Override
    public List<Board> rebuildBoards() {

        try {
            this.removeBoards(listBoards());
            this.addBoards(SystemHelper.listBoards().toArray(new Board[0]));
        } catch (BoardNotFoundException | BoardAlreadyExistsException ignored) {}

        return listBoards();

    }

    @Override
    public void setBoardCOMDriver(@Nonnull String boardSerialNumber, @Nullable COMPort comPort,
                                  int baudRate, int numBitData, int bitStop, String parity, Collection<String> flowControl)
            throws BoardNotFoundException {

        if(comPort==null) return;

        Board board = boards.get(boardSerialNumber);

        if(board==null) {
            throw new BoardNotFoundException();
        }

        synchronized (board.getSerialNumber().intern()) {

            // Se la board ha già un COM port, la stacco e chiudo la connessione, se diversa dalla nuova
            if(board.getComDriver().isPresent() ) {

                if ( board.getComDriver().get().getSerialPort().equals(comPort.getSerialPort()) ) {
                    board.getComDriver().get().changeParameters(baudRate, parity, numBitData, bitStop, flowControl);
                    return;
                } else {
                    board.getComDriver().get().closeCommunication();
                    board.setComDriver(null);
                }

            }

            board.setComDriver(new COMDriver(comPort, baudRate, parity, numBitData, bitStop, flowControl));

        }

    }

    @Override
    public Server addBoards(@Nonnull Board... boards) throws BoardAlreadyExistsException {

        for (Board board : boards) {
            addBoard(board);
        }

        return this;
    }

    @Override
    public Server removeBoard(String boardSerialNumber) throws BoardNotFoundException {

        boardsRWLock.writeLock().lock();
        try {

            Board removedBoard = boards.get(boardSerialNumber);

            if(removedBoard != null) {
                synchronized (removedBoard.getSerialNumber().intern()) {

                    if (boards.remove(boardSerialNumber, removedBoard)) {
                        removedBoard.getComDriver().ifPresent(COMDriver::closeCommunication);
                    } else {
                        throw new BoardNotFoundException("Board '" + boardSerialNumber + "' not found");
                    }

                }
            } else {
                throw new BoardNotFoundException("Board '" + boardSerialNumber + "' not found");
            }

        } finally {
            boardsRWLock.writeLock().unlock();
        }

        return this;

    }

    @Override
    public Server removeBoards(String... serialNumbers) throws BoardNotFoundException {

        boardsRWLock.writeLock().lock();
        try {
            for (String boardId : serialNumbers) {
                this.removeBoard(boardId);
            }
        } finally {
            boardsRWLock.writeLock().unlock();
        }

        return this;
    }


    private Server removeBoards(List<Board> boards) throws BoardNotFoundException {
        boardsRWLock.writeLock().lock();
        try {
            for(Board a : boards){
             this.removeBoard(a.getSerialNumber());
            }
        } finally {
            boardsRWLock.writeLock().unlock();
        }

        return this;
    }


    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public List<Board> listBoards() { return List.copyOf(boards.values()); }

    @Override
    public void removeClientHandler(@Nonnull ClientHandler clientHandler) {
        clientHandlersRWLock.writeLock().lock();
        try {
            clientHandlers.remove(clientHandler.getId(), clientHandler);
        } finally {
            clientHandlersRWLock.writeLock().unlock();
        }
    }

    /**
     * Wait for client connections async
     */
    private void waitForClientsAsync() {
        serverMainThread = new Thread( () -> {

            try {

                while(this.isRunning()) {

                    final Socket socket = serverSocket.accept();

                    logger.info("[waitForClientsAsync] New client connection request received: " + socket);
                    UIPrinterHelper.serverActionPrint("New client connection request received from "+ socket.getInetAddress());

                    new Thread( () -> {

                        // obtain input and output streams
                        try {

                            // Create a new handler object for handling this request.
                            ClientHandler clientHandler = new ClientHandlerImpl(this, socket);

                            addClientHandler(clientHandler);

                            clientHandler.run();

                        } catch (IOException e) {
                            logger.error("[waitForClientsAsync] Error while opening socket streams");
                        }

                    }).start();

                }

            } catch (IOException e) {
                logger.debug("[waitForClientsAsync] Server disconnected.");
            }


        });

        serverMainThread.start();

    }

    @Override
    public @Nullable Board attachBoardOnClient(ClientHandler clientHandler,
                                               String boardSerialNumber)
            throws BoardAlreadyInUseException, BoardNotFoundException {

        Board board;

        boardsRWLock.writeLock().lock();
        try {

            board = boards.get(boardSerialNumber);

            if (board == null) {
                throw new BoardNotFoundException();
            }

            synchronized (board.getSerialNumber().intern()) {

                if (board.isInUse()) {
                    throw new BoardAlreadyInUseException();
                }

                board = clientHandler.attachBoard(board);

            }

        } finally {
            boardsRWLock.writeLock().unlock();
        }

        return board;
    }

    @Override
    public List<ClientHandler> copyOfConnectedClients() {
        return List.copyOf(clientHandlers.values());
    }

    /**
     * Util to add a client handler to the map data struture
     * @param clientHandler ClientHandler client handler to be added, nonnull
     */
    private void addClientHandler(@Nonnull ClientHandler clientHandler) {
        clientHandlersRWLock.writeLock().lock();
        try {
            clientHandlers.put(clientHandler.getId(), clientHandler);
        } finally {
            clientHandlersRWLock.writeLock().unlock();
        }
    }

}
