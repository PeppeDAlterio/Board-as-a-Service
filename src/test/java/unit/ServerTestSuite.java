package unit;

import it.unina.sistemiembedded.exception.BoardAlreadyExistsException;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.server.impl.ServerImpl;
import it.unina.sistemiembedded.utility.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
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
            
            server.removeBoard(testBoard.getSerialNumber());
            
            assertTrue(server.listBoards().isEmpty());
            
        });
        
        assertThrows(BoardNotFoundException.class, () -> server.removeBoard("seriale 123"));
        
    }
    
    @Test
    @DisplayName("Info boards")
    void serverTest6() {
        
        server = new ServerImpl("Server name", 1234);
        assertDoesNotThrow( () -> {
            
            assertTrue(server.listBoards().isEmpty());
            
            Board testBoard = new Board("mia scheda", "seriale 123");
            
            server.addBoard(testBoard);
            
            assertEquals(1, server.listBoards().size());
            
            assertEquals(testBoard,
            server.listBoards().iterator().next());
            
            server.removeBoard(testBoard.getSerialNumber());
            
            assertTrue(server.listBoards().isEmpty());
            
        });
        
        assertThrows(BoardNotFoundException.class, () -> server.removeBoard("seriale 123"));
        
    }
    
    
    @Test
    @DisplayName("ListaTest")
    
    public void ListaBoards() throws IOException {
        int check = 0;
        String buffer_str;
        int i=0;
        ArrayList<Board> list = new ArrayList<Board>();
        
        server = new ServerImpl("Server name", 1234);
        
        do {
            try {
                Process flashProcess = Runtime.getRuntime().exec("." +Constants.STM_PROGRAMMER_PATH+Constants.STM_PROGRAMMER_EXE_NAME + " -c port=swd index="+i);
                flashProcess.waitFor();
                int cnt=0;
                
                cnt = flashProcess.getInputStream().available();
                final byte[] buffer = new byte[cnt];
                flashProcess.getInputStream().read(buffer, 0, cnt);
                System.out.println(new String(buffer));
                buffer_str = new String(buffer);
                
                if(buffer_str.indexOf("Error")!=-1){
                    check = 1;
                    break;
                }
                
                String serialNumber = buffer_str.substring(buffer_str.indexOf("ST-LINK SN  : ") + "ST-LINK SN  : ".length(), buffer_str.indexOf("ST-LINK FW  : ")-1); 
                
                String name = buffer_str.substring(buffer_str.indexOf("Device name : ") + "Device name : ".length(), buffer_str.indexOf("Flash size  : ")-1); 

                list.add(new Board(name, serialNumber));

                // assertDoesNotThrow( () -> {
                //     server.addBoards(new Board(name, serialNumber));
                // });
                
                System.out.println();
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } 
        } while(check!=1);
        
        System.out.println(list);
        //assertEquals(i, list.size());
        //return list;
    }   

}
