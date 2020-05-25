package it.unina.sistemiembedded.model;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class ConnectedClient {

    @Getter @Setter
    private String ip;

    @Getter @Setter
    private String name;

    @Setter
    private Board board;

    @Getter @Setter
    private Date connectedTimestamp;

    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public Optional<Board> getBoard() {
        return Optional.ofNullable(this.board);
    }

    @Override
    public String toString() {
        return "[ NAME = "+name+"  |  IP = "+ip+" ]   Connected at time '"+ formatter.format(connectedTimestamp) +"'\n";
    }
}
