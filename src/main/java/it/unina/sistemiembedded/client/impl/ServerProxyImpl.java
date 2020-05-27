package it.unina.sistemiembedded.client.impl;

import it.unina.sistemiembedded.client.ServerProxy;
import it.unina.sistemiembedded.utility.communication.Commands;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.shared.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
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

    /**
     * Server proxy initialization
     * @param socket Socket server socket
     * @throws IllegalArgumentException socket not connected
     * @throws IOException I/O error occurs when creating the input stream, the socket is closed,
     *                     the socket is not connected, or the socket input has been shutdown using
     */
    public ServerProxyImpl(@Nonnull Socket socket) throws IOException {

        super(socket);

        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());

    }

    @Override
    public String receiveString() throws IOException {
        return this.dis.readUTF();
    }

    @Override
    public int receiveInteger() throws IOException {
        return this.dis.readInt();
    }

    @Override
    public void sendFile(String preMessage, String postMessage, String file, String extension) throws IOException {

        File myFile = new File(file);

        if (!myFile.getName().endsWith(extension.trim()) || !myFile.exists()) {
            logger.error("[sendFile] File not found or not valid: " + myFile.getPath());
            throw new IllegalArgumentException("File not found or not valid: " + myFile.getPath());
        }


        messagingLock.lock();
        try {

            FileInputStream fis = new FileInputStream(myFile);

            if(!StringUtils.isBlank(preMessage)) {
                dos.writeUTF(preMessage);
            }

            dos.writeUTF(Commands.FileTransfer.BEGIN_FILE_TX);

            dos.writeUTF(myFile.getName());

            dos.writeLong(myFile.length());

            long totalCount = 0L;
            int count;
            byte[] buffer = new byte[1024];
            while ((count = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, count);
                dos.flush();
                totalCount += count;
            }

            assert totalCount == myFile.length();

            dos.writeUTF(Commands.FileTransfer.END_OF_FILE_TX);

            fis.close();

            if(!StringUtils.isBlank(postMessage)) {
                dos.writeUTF(postMessage);
            }

        } finally {
            messagingLock.unlock();
        }

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
            if(this.socket.isConnected()) {
                this.socket.close();
            }
        } catch (IOException ignored) {}
        logger.info("[disconnect] Disconnected from server");
    }

}
