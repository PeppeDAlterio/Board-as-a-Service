package it.unina.sistemiembedded.utility;

import java.io.DataOutputStream;
import java.io.IOException;

public class SystemHelper {

    public static void runCommandAndPrintOutputAsync(final String command) {

        new Thread( () -> {
            try {
                runCommandAndPrintOutput(command);
            } catch (IOException ignored) { }
        }).start();

    }

    /**
     * Executes a command in CMD and prints output in Standard Output
     * @param command String command to execute
     * @throws IOException
     */
    public static void runCommandAndPrintOutput(String command) throws IOException {

        Process flashProcess = Runtime.getRuntime().exec(
                "cmd.exe /c " +
                        command
        );

        try {
            flashProcess.waitFor();

            int cnt=0;
            try {
                while ((cnt = flashProcess.getInputStream().available()) > 0) {
                    byte[] buffer = new byte[cnt];
                    flashProcess.getInputStream().read(buffer, 0, cnt);
                    System.out.println(new String(buffer));
                }
                System.out.println();
            } catch (Exception ignored) {}

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static Process remoteDebug(int port, DataOutputStream dos) throws IOException {

        final Process flashProcess = Runtime.getRuntime().exec(
                "."+Constants.GDB_PATH+Constants.GDB_EXE_NAME + " -d -p " + port +
                        " -cp " + "."+Constants.STM_PROGRAMMER_PATH
        );

        new Thread( () -> {

            try {
                while (flashProcess.isAlive()) {
                    int cnt=0;
                    if(((cnt = flashProcess.getInputStream().available()) > 0)) {
                        byte[] buffer = new byte[cnt];
                        flashProcess.getInputStream().read(buffer, 0, cnt);
                        System.out.println(new String(buffer));
                    }
                }
                System.out.println();
            } catch (Exception ignored) {}

        }).start();

        new Thread( () -> {
            try {
                dos.writeUTF(Constants.DEBUG_STARTED);
                System.out.println("Sessione di debug remoto avviata");
                flashProcess.waitFor();
                System.out.println("Sessione di debug remoto terminata.");
                synchronized (dos) {
                    dos.writeUTF(Constants.END_OF_DEBUG);
                }
            } catch (InterruptedException | IOException ignored) { }
        }).start();

        return flashProcess;

    }

}
