package it.unina.sistemiembedded.utility;

import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.ClientHandler;
import it.unina.sistemiembedded.utility.communication.Commands;
import it.unina.sistemiembedded.utility.ui.stream.UIPrinterHelper;
import org.apache.maven.shared.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SystemHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemHelper.class);
    
    private static final Executor executor = Executors.newFixedThreadPool(10);
    
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
    
    //TODO: scrivere descrizione
    public static @Nullable Process remoteDebug(final String boardSerialNumber, final int port, final ClientHandler clientHandler) {
        
        final Process flashProcess;
        try {

            flashProcess = Runtime.getRuntime().exec(
                    "." + Constants.GDB_PATH + Constants.GDB_EXE_NAME
                             + " -d -p " + port + " -i " + boardSerialNumber
                             + " -cp " + "." + Constants.STM_PROGRAMMER_PATH
            );

        } catch (Exception e) {

            logger.error(
                         "There was an error while executing: " + "." + Constants.GDB_PATH + Constants.GDB_EXE_NAME
                         + " -d -p " + port + " -i " + boardSerialNumber
                         + " -cp " + "." + Constants.STM_PROGRAMMER_PATH + ", " + e.getMessage()
            );

            return null;

        }

        Executors.newSingleThreadScheduledExecutor().schedule(() -> {

            if(flashProcess.isAlive()) {

                clientHandler.sendTextMessage(Commands.Debug.STARTED);
                UIPrinterHelper.serverActionPrint("Remote debug session requested by '" + clientHandler.getName() +
                        "' on '" + boardSerialNumber + "' started.");
                logger.info("[remoteDebug] Remote debug session has been started...");

            }

        }, 3, TimeUnit.SECONDS);

        flashProcess.onExit().thenRun(() -> {

            try {

                while (flashProcess.getInputStream().available() > 0) {
                    int cnt;
                    if (((cnt = flashProcess.getInputStream().available()) > 0)) {
                        final byte[] buffer = new byte[cnt];
                        flashProcess.getInputStream().read(buffer, 0, cnt);
                        logger.debug("[remoteDebug]" + new String(buffer));
                    }
                }

            } catch (final Exception ignored) {}

            try {

                flashProcess.waitFor();

                if(flashProcess.exitValue() == -1) {

                    clientHandler.sendTextMessage(Commands.Debug.GDB_BUSY_PORT);

                } else {

                    UIPrinterHelper.serverActionPrint("Remote debug session requested by '" + clientHandler.getName() +
                            "' on '" + boardSerialNumber + "' finished.");
                    logger.info("[remoteDebug] Remote debug session finished.");

                }

            } catch (InterruptedException ignored) {

                logger.info("[remoteDebug] Remote debug session finished.");
                UIPrinterHelper.serverActionPrint("Remote debug session requested by '" + clientHandler.getName() +
                        "' on '" + boardSerialNumber + "' finished.");

                flashProcess.destroyForcibly();

            } finally {

                clientHandler.sendTextMessage(Commands.Debug.FINISHED);

            }

        });

        return flashProcess;

    }

    //TODO: scrivere descrizione
    public static @Nullable Process flash(final String boardSerialNumber, final String elfPath, @Nonnull final ClientHandler clientHandler) {

        if(StringUtils.isBlank(boardSerialNumber) || StringUtils.isBlank(elfPath)) {
            return null;
        }

        final Process flashProcess;
        try {
            flashProcess = Runtime.getRuntime().exec("." + Constants.STM_PROGRAMMER_PATH + Constants.STM_PROGRAMMER_EXE_NAME
            + " -c port=swd sn=" + boardSerialNumber + " -d " + elfPath + " -v --start");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("There was an error while executing: " + "." + Constants.STM_PROGRAMMER_PATH + Constants.STM_PROGRAMMER_EXE_NAME
            + " -c port=swd sn=" + boardSerialNumber + " -d " + elfPath + " -v --start" + ", " + e.getMessage());
            return null;
        }

        UIPrinterHelper.serverActionPrint("Remote flash session requested by '" + clientHandler.getName() +
        "' on '" + boardSerialNumber + "' started.");
        logger.info("[remoteFlash] Remote flash session has been started...");

        flashProcess.onExit().thenRun(() -> {

            try {
                while (flashProcess.getInputStream().available() > 0) {
                    int cnt = 0;
                    if (((cnt = flashProcess.getInputStream().available()) > 0)) {
                        final byte[] buffer = new byte[cnt];
                        flashProcess.getInputStream().read(buffer, 0, cnt);
                        System.out.println(new String(buffer));
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }

        });


        executor.execute(() -> {
            try {
                flashProcess.waitFor();
                UIPrinterHelper.serverActionPrint("Remote flash session requested by '" + clientHandler.getName() +
                "' on '" + boardSerialNumber + "' finished.");
                logger.info("[remoteFlash] Remote flash session finished and the program is started.");
                clientHandler.sendTextMessage(Commands.Flash.SUCCESS);
            } catch (InterruptedException e) {
                logger.info("[remoteFlash] Remote flash session finished.");
                UIPrinterHelper.serverActionPrint("Remote flash session requested by '" + clientHandler.getName() +
                "' on '" + boardSerialNumber + "' finished.");
                clientHandler.sendTextMessage(Commands.Flash.ERROR);
                flashProcess.destroyForcibly();
            }
        });
        
        return flashProcess;
    }

    //TODO: scrivere descrizione
    public static void reset(final String boardSerialNumber, @Nonnull final ClientHandler clientHandler) {

        if(StringUtils.isBlank(boardSerialNumber)) return;

        final Process resetProcess;
        try {
            resetProcess = Runtime.getRuntime().exec("." + Constants.STM_PROGRAMMER_PATH + Constants.STM_PROGRAMMER_EXE_NAME
                    + " -c port=swd sn=" + boardSerialNumber + " -rst");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[reset] There was an error while executing: " + "." + Constants.STM_PROGRAMMER_PATH + Constants.STM_PROGRAMMER_EXE_NAME
                    + " -c port=swd sn=" + boardSerialNumber + " -rst" + ", " + e.getMessage());
            return;
        }

        UIPrinterHelper.serverActionPrint("Board reset requested by '" + clientHandler.getName() +
                "' on '" + boardSerialNumber + "'.");
        logger.info("[reset] Board reset request started...");

        resetProcess.onExit().thenRun(() -> {

            try {
                while (resetProcess.getInputStream().available() > 0) {
                    int cnt = 0;
                    if (((cnt = resetProcess.getInputStream().available()) > 0)) {
                        final byte[] buffer = new byte[cnt];
                        resetProcess.getInputStream().read(buffer, 0, cnt);
                        System.out.println(new String(buffer));
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }

            if(resetProcess.exitValue()==0) {
                UIPrinterHelper.serverActionPrint("Board reset performed by '" + clientHandler.getName() +
                        "' on '" + boardSerialNumber + "' finished.");
                logger.info("[remoteFlash] Board reset performed on '" + boardSerialNumber + "'");
                clientHandler.sendTextMessage(Commands.Reset.SUCCESS);
            } else {
                UIPrinterHelper.serverActionPrint("Error while performing board reset from '" + clientHandler.getName() +
                        "' on '" + boardSerialNumber + "' finished.");
                logger.error("[remoteFlash] Error while performing board reset on '" + boardSerialNumber + "'");
                clientHandler.sendTextMessage(Commands.Reset.ERROR);
            }

        });

    }
    
    /**
    * Executes a command "STM32_Programmer_CLI.exe" in CMD and parse output to
    * store the serial number and name
    *
    *@return List board list
    */
    public static List<Board> listBoards()  {
        String buffer_str;
        int i = 0;
        ArrayList<Board> list = new ArrayList<>();
        
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
                finish = buffer_str.contains("Error") || buffer_str.contains("ST-LINK error");
                
                if(!finish) {
                    
                    String serialNumber = buffer_str.substring(
                    buffer_str.indexOf("ST-LINK SN  : ") + "ST-LINK SN  : ".length(),
                    buffer_str.indexOf("ST-LINK FW  : ") - System.getProperty("line.separator").length());
                    
                    String name = buffer_str.substring(buffer_str.indexOf("Device name : ") + "Device name : ".length(),
                    buffer_str.indexOf("Flash size  : ") - System.getProperty("line.separator").length());
                    
                    list.add(new Board(name, serialNumber));
                    
                    i++;
                    
                }
                
            } catch (IOException|InterruptedException e) {
                logger.error(e.getMessage());
                break;
            }
            
        } while (!finish);
        
        return list;
    }
    
}
