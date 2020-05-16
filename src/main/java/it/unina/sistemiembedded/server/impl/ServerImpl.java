package it.unina.sistemiembedded.server.impl;

import it.unina.sistemiembedded.exception.BoardAlreadyExistsException;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.ClientHandler;
import it.unina.sistemiembedded.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerImpl extends Server {

    private final Logger logger = LoggerFactory.getLogger(ServerImpl.class);

    /**
     * Server running state
     */
    private boolean running = false;

    /**
     * Server boards map: (serial number, board)
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

    private Thread serverMainThread;

    protected ServerImpl(String name) {
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

        clientHandlersRWLock.readLock().lock();
        try {
            this.clientHandlers.values().forEach(ClientHandler::stop);
        } finally {
            clientHandlersRWLock.readLock().unlock();
        }

        clientHandlersRWLock.writeLock().lock();
        try {
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
            if (boards.putIfAbsent(board.getSerialNumber(), board) != null) {
                throw new BoardAlreadyExistsException("Board '" + board.getSerialNumber() + "' already exists");
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
    public Server removeBoard(String serialNumber) throws BoardNotFoundException {

        boardsRWLock.writeLock().lock();
        try {
            if (!boards.remove(serialNumber, boards.get(serialNumber))) {
                throw new BoardNotFoundException("Board '" + serialNumber + "' not found");
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
    public Collection<Board> listBoards() {
        return boards.values();
    }

    /**
     * Wait for client connections async
     */
    private void waitForClientsAsync() {
        serverMainThread = new Thread( () -> {

            try {

                while(this.isRunning()) {

                    Socket socket = serverSocket.accept();

                    logger.info("[waitForClientsAsync] New client connection request received: " + socket);

                    // obtain input and output streams
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                    // Create a new handler object for handling this request.
                    ClientHandler clientHandler = new ClientHandlerImpl(clientSequencer.getAndIncrement(),
                            this, socket, dis, dos);

                    addClientHandler(clientHandler);

                    clientHandler.run();

                }

            } catch (IOException e) {
                logger.debug("[waitForClientsAsync] Server disconnected.");
            }


        });

        serverMainThread.start();

    }

    private void addClientHandler(ClientHandler clientHandler) {
        clientHandlersRWLock.writeLock().lock();
        try {
            clientHandlers.put(clientHandler.getId(), clientHandler);
        } finally {
            clientHandlersRWLock.writeLock().unlock();
        }
    }

}
