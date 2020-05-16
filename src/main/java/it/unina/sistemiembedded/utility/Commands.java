package it.unina.sistemiembedded.utility;

public class Commands {

    /**
     * ATTACH ON BOARD
     */
    public static class AttachOnBoard {

        public static class Request {
            public static final String REQUEST          = "$--- ATTACH ON BOARD REQUEST ---$";
            public static final String TRANSFER_BOARD   = "$--- ATTACH ON BOARD REQUEST TRANSFER BOARD ---$";
        }

        public static class Response {
            public static final String SUCCESS          = "$--- ATTACH ON BOARD SUCCESS ---$";
            public static final String BOARD_NOT_FOUND  = "$--- ATTACH ON BOARD NOT FOUND ---$";
            public static final String BOARD_BUSY       = "$--- ATTACH ON BOARD BUSY ---$";
            public static final String ERROR            = "$--- ATTACH ON BOARD GENERIC ERROR ---$";
        }
    }

    /**
     * DETACH FROM BOARD
     */
    public static class DetachFromBoard {

        public static class Request {
            public static final String REQUEST          = "$--- DETACH FROM BOARD REQUEST ---$";
        }

        public static class Response {
            public static final String SUCCESS          = "$--- DETACH FROM BOARD SUCCESS ---$";
            public static final String ATTACH_NOT_FOUND = "$--- DETACH FROM BOARD NOT FOUND ---$";
            public static final String GENERIC_ERROR    = "$--- DETACH FROM BOARD GENERIC ERROR ---$";
        }

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
