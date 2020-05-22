package it.unina.sistemiembedded.utility.communication;

public class Commands {

    /**
     * ATTACH ON BOARD
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
         * Sent by both Client and Server to successfully acknowledge a request
         */
        public static final String SUCCESS                  = "$--- ATTACH ON BOARD SUCCESS ---$";

        /**
         * Sent by Server as a "board not found" response to a board request
         */
        public static final String BOARD_NOT_FOUND          = "$--- ATTACH ON BOARD NOT FOUND ---$";

        /**
         * Sent by Server as a "board already in use" response to a board request
         */
        public static final String BOARD_BUSY               = "$--- ATTACH ON BOARD BUSY ---$";

        /**
         * Sent by both Client and Server to acknowledge a request with an error result
         */
        public static final String ERROR                    = "$--- ATTACH ON BOARD GENERIC ERROR ---$";

    }

    /**
     * DETACH FROM BOARD
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
        public static final String ERROR          = "$--- FLASH ON BOARD ERROR ---$";

    }

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

    }

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
     * BOARD COMMUNICATION
     */
    public static class BoardCommunication {

        public static class Request {
            public static final String FLASH_REQUEST    = "$--- BOARD FLASH REQUEST ---$";
            public static final String DEBUG_REQUEST    = "$--- BOARD DEBUG REQUEST ---$";
        }

        public static class Response {

            public static final String FLASH_SUCCESS    = "$--- BOARD FLASH SUCCESS ---$";
            public static final String FLASH_ERROR      = "$--- BOARD FLASH ERROR ---$";

            public static final String DEBUG_SUCCESS    = "$--- BOARD DEBUG SUCCESS ---$";
            public static final String DEBUG_ERROR      = "$--- BOARD DEBUG ERROR ---$";

        }

    }

}
