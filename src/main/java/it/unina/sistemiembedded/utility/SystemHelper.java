package it.unina.sistemiembedded.utility;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unina.sistemiembedded.client.impl.ClientImpl;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.impl.ServerImpl;

public class SystemHelper {

    public static void runCommandAndPrintOutputAsync(final String command) {

        new Thread(() -> {
            try {
                runCommandAndPrintOutput(command);
            } catch (final IOException ignored) {
            }
        }).start();

    }

    /**
     * Executes a command in CMD and prints output in Standard Output
     * 
     * @param command String command to execute
     * @throws IOException
     */
    public static void runCommandAndPrintOutput(final String command) throws IOException {

        final Process flashProcess = Runtime.getRuntime().exec("cmd.exe /c " + command);

        try {
            flashProcess.waitFor();

            int cnt = 0;
            try {
                while ((cnt = flashProcess.getInputStream().available()) > 0) {
                    final byte[] buffer = new byte[cnt];
                    flashProcess.getInputStream().read(buffer, 0, cnt);
                    System.out.println(new String(buffer));
                }
                System.out.println();
            } catch (final Exception ignored) {
            }

        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static Process remoteDebug(final int port, final DataOutputStream dos) throws IOException {

        final Process flashProcess = Runtime.getRuntime().exec("." + Constants.GDB_PATH + Constants.GDB_EXE_NAME
                + " -d -p " + port + " -cp " + "." + Constants.STM_PROGRAMMER_PATH);

        new Thread(() -> {

            try {
                while (flashProcess.isAlive()) {
                    int cnt = 0;
                    if (((cnt = flashProcess.getInputStream().available()) > 0)) {
                        final byte[] buffer = new byte[cnt];
                        flashProcess.getInputStream().read(buffer, 0, cnt);
                        System.out.println(new String(buffer));
                    }
                }
                System.out.println();
            } catch (final Exception ignored) {
            }

        }).start();

        new Thread(() -> {
            try {
                dos.writeUTF(Constants.DEBUG_STARTED);
                System.out.println("Sessione di debug remoto avviata");
                flashProcess.waitFor();
                System.out.println("Sessione di debug remoto terminata.");
                synchronized (dos) {
                    dos.writeUTF(Constants.END_OF_DEBUG);
                }
            } catch (InterruptedException | IOException ignored) {
            }
        }).start();

        return flashProcess;

    }

    /**
     * Executes a command "STM32_Programmer_CLI.exe" in CMD and parse output to
     * store the serial number and name
     *
     */
    public static List<Board> listBoards()  {
        String buffer_str;
        int i = 0;
        ArrayList<Board> list = new ArrayList<Board>();

        list.toArray(new Board[0]);

        boolean finish = false;
        do {
            try {
                Process flashProcess = Runtime.getRuntime().exec("." + Constants.STM_PROGRAMMER_PATH
                        + Constants.STM_PROGRAMMER_EXE_NAME + " -c port=swd index=" + i);
                flashProcess.waitFor();
                int cnt = flashProcess.getInputStream().available();
                byte[] buffer = new byte[cnt];
                flashProcess.getInputStream().read(buffer, 0, cnt);
                buffer_str = new String(buffer);
                
                finish = buffer_str.contains("Error");
                
                if(!finish) {

                    String serialNumber = buffer_str.substring(
                            buffer_str.indexOf("ST-LINK SN  : ") + "ST-LINK SN  : ".length(),
                            buffer_str.indexOf("ST-LINK FW  : ") - 1);

                    String name = buffer_str.substring(buffer_str.indexOf("Device name : ") + "Device name : ".length(),
                            buffer_str.indexOf("Flash size  : ") - 1);

                    list.add(new Board(name, serialNumber));

                    System.out.println();
                    i++;
                }
            } catch (IOException|InterruptedException e) {
                e.printStackTrace();
            }

        } while (!finish);

        return list;
    }
    

    public void flashBoard(){


    }

    /**
     * Executes a command "STM32_Programmer_CLI.exe" in CMD and parse output to
     * store the serial number and name
     * 
     * @param br Baudrate
     * @param P parity
     * @param db Bata Bit
     * @param sb Stop Bit
     * @param fc Flow Control
     * @return none
     * @throws IOException
     */
    public void com (String COM, int br, String P, int db, int sb, String fc){
        try {
            Process flashProcess = Runtime.getRuntime()
                    .exec("." + Constants.STM_PROGRAMMER_PATH + Constants.STM_PROGRAMMER_EXE_NAME + " -c port=" + COM
                            + " br=" + br + " P=" + P + " db=" + db + " sb=" + sb + " fc=" + fc);
            
            flashProcess.waitFor();
            int cnt = 0;
            cnt = flashProcess.getInputStream().available();
            byte[] buffer = new byte[cnt];
            flashProcess.getInputStream().read(buffer, 0, cnt);
            System.out.println(new String(buffer));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
   
}
