package it.unina.sistemiembedded.server.impl;

import it.unina.sistemiembedded.exception.BoardAlreadyExistsException;
import it.unina.sistemiembedded.exception.BoardAlreadyInUseException;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.ClientHandler;
import it.unina.sistemiembedded.server.Server;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
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
     * Server boards map: (id, board)
     */
    private final Map<String, Board> boards = new HashMap<>();

    /**
     * Server client handlers map: (id, client handler)
     */
    private final Map<Long, ClientHandler> clientHandlers = new HashMap<>();

    /**
     * Clients sequencer for IDs
     */
    private final AtomicLong clientSequencer = new AtomicLong(0);

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

            this.running = true;

        }

        waitForClientsAsync();

    }

    @Override
    public void stop() throws IOException {

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

        logger.info("[stop] Server successfully stopped.");

        this.running = false;

    }

    @Override
    public Server addBoard(@Nonnull Board board) throws BoardAlreadyExistsException {

        boardsRWLock.writeLock().lock();
        try {
            if (boards.putIfAbsent(board.getId(), board) != null) {
                throw new BoardAlreadyExistsException("Board '" + board.getId() + "' already exists");
            }
        } finally {
            boardsRWLock.writeLock().unlock();
        }

        return this;

    }

    @Override
    public Server addBoards(@Nonnull Board... boards) throws BoardAlreadyExistsException {

        for (Board board : boards) {
            addBoard(board);
        }

        return this;
    }

    @Override
    public Server removeBoard(String boardId) throws BoardNotFoundException {

        boardsRWLock.writeLock().lock();
        try {
            if (!boards.remove(boardId, boards.get(boardId))) {
                throw new BoardNotFoundException("Board '" + boardId + "' not found");
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
    public List<Board> listBoards() {
        return new ArrayList<>(boards.values());
    }

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

                    new Thread( () -> {

                        // obtain input and output streams
                        try {

                            DataInputStream dis = new DataInputStream(socket.getInputStream());
                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                            // Create a new handler object for handling this request.
                            ClientHandler clientHandler = new ClientHandlerImpl(clientSequencer.getAndIncrement(),
                                    this, socket, dis, dos);

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
    public boolean existsBoardBySerialNumber(String serialNumber) {
        return boards.values().parallelStream().anyMatch(board -> board.getSerialNumber().equalsIgnoreCase(serialNumber));
    }

    @Override
    public boolean existsBoardById(String boardId) {
        return false;
    }

    @Override
    public @Nullable Board attachBoardOnClient(ClientHandler clientHandler,
                                               String boardId)
            throws BoardAlreadyInUseException, BoardNotFoundException {

        Board board;

        boardsRWLock.writeLock().lock();
        try {

            board = boards.get(boardId);

            if (board == null) {
                throw new BoardNotFoundException();
            }

            synchronized (board) {

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
