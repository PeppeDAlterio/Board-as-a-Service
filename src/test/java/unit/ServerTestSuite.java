package unit;

import it.unina.sistemiembedded.exception.BoardAlreadyExistsException;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.server.impl.ServerImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTestSuite {

    private Server server;

    @AfterEach
    void cleanup() throws IOException {
        if(server!=null) {
            server.stop();
        }
    }

    @Test
    @DisplayName("Server test:")
    void serverTest1() {

        server = new ServerImpl("Server name", 1234);
        assertDoesNotThrow(server::stop);
        assertDoesNotThrow(server::start);
        assertDoesNotThrow(server::start);
        assertDoesNotThrow(server::stop);

    }

    @Test
    @DisplayName("Server test: invalid port check")
    void serverTest2() {

        assertThrows(IllegalArgumentException.class, () -> new ServerImpl("Server name", 66000));

        assertThrows(IllegalArgumentException.class, () -> new ServerImpl("Server name", -1));

        assertDoesNotThrow(() ->
                new ServerImpl("Server name", ThreadLocalRandom.current().nextInt(0, 65535+1)));

    }

    @Test
    @DisplayName("Server test: busy port")
    void serverTest3() {

        server = new ServerImpl("Server name", 1234);
        assertDoesNotThrow(()->server.start());
        assertThrows(IOException.class, () -> new ServerImpl("server name", 1234).start());

    }

    @Test
    @DisplayName("Server test: add multiple boards")
    void serverTest4() {

        server = new ServerImpl("Server name", 1234);
        assertDoesNotThrow( () -> {
            server.addBoards(new Board("mia scheda 1","seriale 123"),
                             new Board("mia scheda 2","seriale 456"),
                             new Board("mia scheda 3", "seriale 789"));

            assertEquals(3, server.listBoards().size());

        });

        assertThrows(BoardAlreadyExistsException.class,
                () -> server.addBoard(new Board( "mia scheda 3","seriale 123")));

    }

    @Test
    @DisplayName("Server test: simple add and remove board")
    void serverTest5() {

        server = new ServerImpl("Server name", 1234);
        assertDoesNotThrow( () -> {

            assertTrue(server.listBoards().isEmpty());

            Board testBoard = new Board("mia scheda", "seriale 123");

            server.addBoard(testBoard);

            assertEquals(1, server.listBoards().size());

            assertEquals(testBoard,
                    server.listBoards().iterator().next());

            server.removeBoard(testBoard.getId());

            assertTrue(server.listBoards().isEmpty());

        });

        assertThrows(BoardNotFoundException.class, () -> server.removeBoard("seriale 123"));

    }

}
