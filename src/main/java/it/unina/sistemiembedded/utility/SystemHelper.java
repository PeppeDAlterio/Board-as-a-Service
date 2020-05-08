package it.unina.sistemiembedded.utility;

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

}
