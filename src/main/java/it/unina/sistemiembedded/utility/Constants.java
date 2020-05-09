package it.unina.sistemiembedded.utility;

public class Constants {

    public static final String GDB_PATH = "\\tools\\GDB\\";

    public static final String GDB_EXE_NAME = "ST-LINK_gdbserver.exe";

    public static final String STM_PROGRAMMER_PATH = "\\tools\\STM32CubeProgrammer\\bin\\";

    public static final String STM_PROGRAMMER_EXE_NAME = "STM32_Programmer_CLI.exe";

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


    /**
     * Sent by the server to request a remote debug session.
     */
    public static final String BEGIN_OF_DEBUG = "--- BEGIN OF DEBUG ---";
    /**
     * Sent by the client at the beginning of a remote debug session
     */
    public static final String DEBUG_STARTED = "--- BEGIN OF DEBUG ---";
    /**
     * Sent by the client at the end of the debugging session.
     */
    public static final String END_OF_DEBUG = "--- END OF DEBUG ---";

}
