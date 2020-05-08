package it.unina.sistemiembedded.utility;

public class Constants {

    /**
     * Sent by the sender at the beginning of the file transfer process
     */
    public static final String BEGIN_FILE_TX = "--- BEGIN OF FILE TX ---";
    /**
     * Sent by the sender at the end of the file transfer process
     */
    public static final String END_FILE_TX = "--- END OF FILE TX ---";

    /**
     * Sent by the server at the beginning of the flash process.
     */
    public static final String BEGIN_OF_REMOTE_FLASH = "--- BEGIN OF REMOTE FLASH ---";
    /**
     * Sent by the client at the end of the flash process.
     */
    public static final String END_OF_REMOTE_FLASH = "--- END OF REMOTE FLASH ---";

}
