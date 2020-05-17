package it.unina.sistemiembedded.client.impl;

import it.unina.sistemiembedded.client.ServerProxy;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.shared.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter @Setter
public class ServerProxyImpl extends ServerProxy {

    private final Logger logger = LoggerFactory.getLogger(ServerProxyImpl.class);

    private final DataInputStream dis;
    private final DataOutputStream dos;

    /**
     * Lock for DataOutputStream
     */
    private final Lock messagingLock = new ReentrantLock(true);


    public ServerProxyImpl(@Nonnull Socket socket) throws IOException {

        super(socket);

        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());

    }

    @Override
    public String receive() throws IOException {
        return this.dis.readUTF();
    }


    @Override
    public void sendMessages(String ... messages) {

        messagingLock.lock();
        try {
            for (String m : messages) {
                sendMessage(m);
            }
        } finally {
            messagingLock.unlock();
        }


    }

    @Override
    public void sendMessage(String msg) {

        if(!isConnected() || StringUtils.isBlank(msg)) return;

        messagingLock.lock();
        try {
            logger.debug("[sendMessage] Sending message: " + msg);
            this.dos.writeUTF(msg);
        } catch (IOException e) {
            logger.error("[sendMessage] Connection lost.");
            this.disconnect();
        } finally {
            messagingLock.unlock();
        }

    }

    @Override
    public boolean isConnected() {
        return this.socket.isConnected();
    }

    @Override
    public void disconnect() {
        try {
            this.socket.close();
        } catch (IOException ignored) {}
        logger.info("[disconnect] Disconnected from server");
    }

}
