package it.unina.sistemiembedded.utility.communication;

public class Commands {

    /**
     * Attach on board protocol commands
     */
    public static class AttachOnBoard {

        /**
         * Sent by Client to Request a board by serial number
         */
        public static final String REQUEST_BOARD            = "$--- ATTACH ON BOARD REQUEST ---$";

        /**
         * Sent by Server to begin board transfer
         */
        public static final String BEGIN_TRANSFER_BOARD     = "$--- ATTACH ON BOARD REQUEST TRANSFER BOARD ---$";

        /**
         * Sent by the Client to successfully acknowledge the transfer request
         */
        public static final String SUCCESS                  = "$--- ATTACH ON BOARD SUCCESS ---$";

        /**
         * Sent by Server as a "board not found" response to a board request
         */
        public static final String BOARD_NOT_FOUND          = "$--- ATTACH ON BOARD NOT FOUND ---$";

        /**
         * Sent by Server as a "board already in use" response to a board request
         */
        public static final String BOARD_BUSY = "$--- ATTACH ON BOARD BUSY ---$";

        /**
         * Sent by both Client and Server to acknowledge a request with an error result
         */
        public static final String ERROR                    = "$--- ATTACH ON BOARD GENERIC ERROR ---$";

    }

    /**
     * Detach from board protocol commands
     */
    public static class DetachFromBoard {

        /**
         * Sent by the client
         */
        public static final String REQUEST          = "$--- DETACH FROM BOARD REQUEST ---$";

        /**
         * Sent by the Server to acknowledge the detach
         */
        public static final String SUCCESS          = "$--- DETACH FROM BOARD SUCCESS ---$";

    }

    /**
     * Flash protocol commands
     */
    public static class Flash {

        /**
         * Sent by the client to request the flash
         */
        public static final String REQUEST          = "$--- FLASH ON BOARD REQUEST ---$";

        /**
         * Sent by the Server to acknowledge the flash
         */
        public static final String SUCCESS          = "$--- FLASH ON BOARD SUCCESS ---$";

        /**
         * Sent by the Server to acknowledge an error with the flash
         */
        public static final String ERROR            = "$--- FLASH ON BOARD ERROR ---$";

    }

    /**
     * Debug session protocol commands
     */
    public static class Debug {

        /**
         * Sent by the client to request the debug
         */
        public static final String REQUEST          = "$--- DEBUG ON BOARD REQUEST ---$";

        /**
         * Sent by the server when the debugging session has been started
         */
        public static final String STARTED         = "$--- DEBUG ON BOARD STARTED ---$";

        /**
         * Sent by the server when the debugging session finishes
         */
        public static final String FINISHED         = "$--- DEBUG ON BOARD FINISHED ---$";

        /**
         * Sent by the Server as ready to debug
         */
        public static final String READY_TO_DEBUG   = "$--- DEBUG ON BOARD READY ---$";

        /**
         * Sent by the Server to acknowledge an error with the debug process
         */
        public static final String ERROR            = "$--- DEBUG ON BOARD ERROR ---$";

        /**
         * Sent by the client to request end of the debug
         */
        public static final String REQUEST_END      = "$--- DEBUG ON BOARD REQUEST END ---$";

        /**
         * Sent by the server if GDB returns a busy port error
         */
        public static final String GDB_BUSY_PORT    = "$--- DEBUG ON BOARD GDB BUSY PORT ---$";

        /**
         * Sent by the server if GDB returns an error
         */
        public static final String GDB_ERROR        = "$--- DEBUG ON BOARD GDB ERROR ---$";
    }

    /**
     * Info request protocol commands
     */
    public static class Info {

        /**
         * Sent by the client to request server's board list
         */
        public static final String BOARD_LIST_REQUEST          = "$--- INFO BOARD LIST REQUEST ---$";

        /**
         * Sent by the Server before sending board list
         */
        public static final String BEGIN_OF_BOARD_LIST         = "$--- INFO BEGIN OF BOARD LIST ---$";

    }

    /**
     * Interrupt messages protocol commands
     */
    public static class Interrupt {
        
        /**
         * Send by Server to all connected clients on disconnect
         */
        public static final String SERVER_DISCONNECTED         = "$--- INTERRUPT SERVER DISCONNECTED ---$";

    }

    /**
     * File transfer protocol commands
     */
    public static class FileTransfer {
        /**
         * Sent by Client at the beginning of file transfer
         */
        public static final String BEGIN_FILE_TX  = "$--- FLASH ON BOARD BEGIN FILE TX ---$";

        /**
         * Sent by Client on file transfer completed
         */
        public static final String END_OF_FILE_TX  = "$--- FLASH ON BOARD END OF FILE TX ---$";
    }

    /**
     * Board software reset
     */
    public static class Reset {

        /**
         * Sent by the client to request board software reset
         */
        public static final String REQUEST                     = "$--- BOARD SOFTWARE RESET REQUEST ---$";

        /**
         * Sent by the server to acknowledge software reset with success
         */
        public static final String SUCCESS                     = "$--- BOARD SOFTWARE RESET SUCCESS ---$";

        /**
         * Sent by the server to acknowledge software reset with success
         */
        public static final String ERROR                       = "$--- BOARD SOFTWARE RESET ERROR ---$";

    }

}
