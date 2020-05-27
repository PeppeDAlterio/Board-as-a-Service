package it.unina.sistemiembedded.utility.ui.stream;

import lombok.Getter;

import java.io.PrintStream;

public class UIPrinterHelper {

    @Getter
    public enum TextArea {
        /**
         * Descrizione
         */
        SERVER_COMMUNICATION("TEXT_AREA_COMUNICATION_SERVER"),
        /**
         * Descrizione
         */
        SERVER_ACTION("TEXT_AREA_ACTION_SERVER"),
        /**
         *
         */
        CLIENT_MESSAGE("TEXT_AREA_SENDMESSAGE_CLIENT"),
        /**
         *
         */
        CLIENT_DEBUG("TEXT_AREA_DEGUB_CLIENT"),
        /**
         *
         */
        CLIENT_FLASH("TEXT_AREA_FLASH_CLIENT");

        private String value;

        TextArea(String value) {
            this.value = value;
        }
    }

    /**
     * Use this to configure the PrintStream to redirect the output to
     * @param printStream PrintStream to redirect the output to
     */
    public static void setPrintStream(PrintStream printStream) {
        System.setOut(printStream);
    }

    /**
     * Write a message in a TextArea
     * @param textArea TextArea
     * @param msg String message
     */
    private static void write(TextArea textArea, String msg) {
        System.out.println(textArea.getValue() + msg);
    }

    public static void serverCommunicationPrint(String msg) {
        write(TextArea.SERVER_COMMUNICATION, msg);
    }

    public static void serverActionPrint(String msg) {
        write(TextArea.SERVER_ACTION, msg);
    }

    public static void clientMessage(String msg) {
        write(TextArea.CLIENT_MESSAGE, msg);
    }

    public static void clientDebug(String msg) { write(TextArea.CLIENT_DEBUG, msg); }

    public static void clientFlash(String msg) {
        write(TextArea.CLIENT_FLASH, msg);
    }

}
