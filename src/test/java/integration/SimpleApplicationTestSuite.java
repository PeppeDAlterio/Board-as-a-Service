package integration;

import it.unina.sistemiembedded.client.impl.ClientImpl;
import it.unina.sistemiembedded.exception.BoardAlreadyExistsException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.impl.ServerImpl;
import it.unina.sistemiembedded.utility.Commands;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleApplicationTestSuite {

    private ServerImpl server;

    //TODO: change to Client
    private ClientImpl client;

    private final String serialNumberBoard1 = "serialNumberBoard1";
    private final String serialNumberBoard2 = "serialNumberBoard2";
    private Board testBoard1, testBoard2;

    @BeforeEach
    void init() throws IOException, BoardAlreadyExistsException {

        Properties properties = new Properties();
        properties.putAll(System.getProperties());
        properties.put("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        System.setProperties(properties);

        server = new ServerImpl("Mio server");

        testBoard1 = new Board(serialNumberBoard1, null);
        testBoard2 = new Board(serialNumberBoard2, null);

        server.addBoards(testBoard1, testBoard2);
        server.start();

        assertTrue(server.isRunning());

        assertEquals(2, server.listBoards().size());

        server.getBoards().forEach((k, v) -> {
            if(k.equals(serialNumberBoard1)) {
                assertSame(v, testBoard1);
            } else if(k.equals(serialNumberBoard2)) {
                assertSame(v, testBoard2);
            }
        });

    }

    @AfterEach
    void cleanup() throws IOException {
        this.server.stop();
    }

    @Test
    @DisplayName("Client connection")
    public void clientConnectTest1() {

        assertEquals(0, server.getClientHandlers().size());

        client = new ClientImpl("My name");
        assertDoesNotThrow( () -> client.connect("127.0.0.1") );

        assertEquals(1, server.getClientHandlers().size());

    }

    @Test
    @DisplayName("Client connection and attach on a board")
    public void clientConnectAndAttachTest1() {

        assertEquals(0, server.getClientHandlers().size());

        client = new ClientImpl("My name");
        assertDoesNotThrow( () -> client.connect("127.0.0.1") );

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, server.getClientHandlers().size());

        assertDoesNotThrow( () -> client.attachOnBoardRequest(serialNumberBoard1));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertNotNull(client.getConnectedBoard());
        assertEquals(testBoard1, client.getConnectedBoard());
        assertTrue(server.getBoards().get(serialNumberBoard1).isInUse());

    }

    @Test
    public void test() throws IOException, BoardAlreadyExistsException {

        server = new ServerImpl("ciao");
        client = new ClientImpl("giuseppe");

        server.start();
        server.addBoard(new Board("1234", null));

        System.out.println(server.getClientHandlers());

        client.connect("127.0.0.1");

        client.sendMessage("we");

        client.sendMessage(Commands.AttachOnBoard.Request.REQUEST);
        client.sendMessage("1234");

        client.sendMessage(Commands.AttachOnBoard.Request.REQUEST);
        client.sendMessage("1234");

        client.sendMessage(Commands.AttachOnBoard.Request.REQUEST);
        client.sendMessage("12345");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
