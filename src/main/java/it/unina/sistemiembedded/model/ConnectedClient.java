package it.unina.sistemiembedded.model;

import it.unina.sistemiembedded.utility.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class ConnectedClient {

    @Getter @Setter
    public static class ConnectedBoard {

        private String name;

        private String serialNumber;

        private boolean inUse = false;

        private boolean debugging = false;

        @Override
        public String toString() {
            return "[ NAME = " + name + "  |  SERIAL NUMBER = " + serialNumber + "  |  IN DEBBUGGING = " + debugging + " ]\n";
        }
    }

    @Getter @Setter
    private String ip;

    @Getter @Setter
    private String name;

    @Setter
    private Board board;

    @Getter @Setter
    private Date connectedTimestamp;

    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public Optional<ConnectedBoard> getBoard() {
        return Optional.ofNullable(ObjectMapper.map(this.board, ConnectedBoard.class));
    }

    @Override
    public String toString() {
        return "[ NAME = "+name+"  |  IP = "+ip+" ]   Connected at time '"+ formatter.format(connectedTimestamp) +"'\n";
    }
}
