package it.unina.sistemiembedded.utility.debug;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter @Setter
public class GDBProcess {

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);

    private Process delegate;

    private String ip;
    private int port;

    public GDBProcess(Process delegate, String ip, int port) {
        this.delegate = delegate;
        this.ip = ip;
        this.port = port;
    }

    public @Nullable Process stopDebug() {
        if(delegate!=null && delegate.isAlive()) {
            try {
                new Socket(ip, port).close();
            } catch (IOException ignored) {
            } finally {
                executor.schedule(() -> {
                    if (delegate.isAlive()) {
                        delegate.destroyForcibly();
                    }
                }, 3, TimeUnit.SECONDS);
            }
        }
        return delegate;
    }

}
