package it.unina.sistemiembedded.model;

import lombok.Getter;
import lombok.Setter;

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

    public Optional<Board> getBoard() {
        return Optional.ofNullable(this.board);
    }

}
