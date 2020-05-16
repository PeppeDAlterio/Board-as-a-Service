package it.unina.sistemiembedded.server.impl;

import it.unina.sistemiembedded.server.ClientHandler;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.Constants;
import org.apache.maven.shared.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandlerImpl extends ClientHandler {

    private final Logger logger = LoggerFactory.getLogger(ClientHandlerImpl.class);

    /**
     * Client name
     */
    private String name;

    /**
     * Client running state. Please not that just the desiderata state, socket may be open/closed.
     * The isAlive() method concretely checks handler's alive status.
     */
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

        server.removeClientHandler(this);

    }

    @Override
    public void run() {

        if(socket.isClosed()) return;

        this.running = socket.isConnected();

        logger.info("[run] Client handler (" + this.id + ") has been started");

        try {
            this.name = dis.readUTF();
            logger.debug("[run] ");
        } catch (IOException e) {
            e.printStackTrace();
            this.stop();
            return;
        }

        while(isAlive()) {
            try
            {
                // receive and parse a string message ...
                parseReceivedMessage(dis.readUTF());

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

        }

        this.stop();

    }

    @Override
    public boolean isAlive() {
        return this.running && socket.isConnected();
    }

    private String parseReceivedMessage(String message) {

        if(StringUtils.isBlank(message)) return "";

        logger.debug("[parseReceivedMessage] Beggining parse of message: " + message);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[ Client (").append(this.id).append(", ").append(this.name).append(") ] ");

        switch (message) {

            case Constants.END_OF_REMOTE_FLASH:

                stringBuilder.append(" Flash remoto completato.");

                break;

            case Constants.BEGIN_OF_DEBUG:

                stringBuilder.append("Sessione di debug remoto avviata. Utilizza l'ambiente di sviluppo verso questo IP: ")
                        .append(this.socket.getInetAddress())
                        .append(" con il porto specificato in precedenza.");

                break;

            case Constants.END_OF_DEBUG:

                stringBuilder.append("Sessione di debug remoto terminata.");

                break;

            default:

                stringBuilder.append("Ricevuto: ").append(message);

                break;

        }

        logger.info("[parseReceivedMessage] " + stringBuilder.toString());

        return stringBuilder.toString();

    }

}
