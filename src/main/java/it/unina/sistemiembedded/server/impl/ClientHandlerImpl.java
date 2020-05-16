package it.unina.sistemiembedded.server.impl;

import it.unina.sistemiembedded.server.ClientHandler;
import it.unina.sistemiembedded.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandlerImpl extends ClientHandler {

    private final Logger logger = LoggerFactory.getLogger(ClientHandlerImpl.class);

    private boolean running;

    /**
     * Create a new Client Handler
     *
     * @param id     long client id
     * @param server Server Server that handles the client
     * @param socket Socket Socket connected to the client
     * @param dis    DataInputStream Input stream of the client
     * @param dos    DataOutputStream Output stream of the client
     */
    protected ClientHandlerImpl(long id, Server server, Socket socket, DataInputStream dis, DataOutputStream dos) {
        super(id, server, socket, dis, dos);
    }

    @Override
    public void stop() {

        this.running = false;

        try {
            if(this.socket.isConnected()) {
                this.socket.close();
            }
            logger.info("[stop] Client handler (" + this.id + ") has been stopped");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("[stop] There was an error while closing client handler (" + this.id +" socket");
        }

    }

    @Override
    public void run() {

        if(socket.isClosed()) return;

        this.running = socket.isConnected();

        logger.info("[run] Client handler (" + this.id + ") has been started");

        while(isAlive()) {
            // todo...
        }

    }

    public boolean isAlive() {
        return this.running && socket.isConnected();
    }

}
