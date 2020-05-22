package it.unina.sistemiembedded.utility.ui;

import lombok.Getter;

import java.io.PrintStream;

public class UIHelper {

    public static final String TEXT_AREA_COMUNICATION_SERVER = "TEXT_AREA_COMUNICATION_SERVER";
    public static final String TEXT_AREA_ACTION_SERVER = "TEXT_AREA_ACTION_SERVER";
    public static final String TEXT_AREA_SENDMESSAGE_CLIENT ="TEXT_AREA_SENDMESSAGE_CLIENT";
    public static final String TEXT_AREA_DEGUB_CLEINT ="TEXT_AREA_DEGUB_CLEINT";
    public static final String TEXT_AREA_FLASH_CLIENT ="TEXT_AREA_FLASH_CLIENT";

    @Getter
    public enum TextArea {
        /**
         * Descrizione
         */
        SERVER_COMMUNICATION("TEXT_AREA_COMUNICATION_SERVER"),
        /**
         * Descrizione
         */
        SERVER_ACTION("TEXT_AREA_ACTION_SERVER");

        private String value;

        TextArea(String value) {
            this.value = value;
        }
    }

    public static void setPrintStream(PrintStream printStream) {
        System.setOut(printStream);
    }

    public static void serverCommunicationPrint(String msg) {
        write(TextArea.SERVER_COMMUNICATION, msg);
    }

    private static void write(TextArea textArea, String msg) {
        System.out.println(textArea.getValue() + "Remote debug session finished");
    }

    /*
        Esempio uso:
        UIHelper.serverCommunicationPrint("Prova prova 123");
        anzichè fare
        System.out.println(RedirectStream.TEXT_AREA_COMUNICATION_SERVER + "Prova prova 123");

        così se domani abbiamo un problema / vogliamo cambiarlo / non so, teniamo un singolo metodo che viene invocato

        Nel main invece di fare System.setOut(printStream)
        facciamo
        UIHelper.setPrintStream(printStream)
        per lo stesso motivo di prima

     */

}
