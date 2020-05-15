package it.unina.sistemiembedded.model;

import com.sun.istack.internal.NotNull;
import org.apache.maven.shared.utils.StringUtils;

import javax.annotation.Nonnull;
import java.util.Objects;

public class Board {

    /**
     * Board symbolic name
     */
    private String name;

    /**
     * Global unique serial number
     */
    private @NotNull final String serialNumber;

    /**
     * Board password. Used to authorize the connection
     */
    private String password;

    /**
     * Create a new board with a given name, serial number and password.
     * @param name String board name
     * @param serialNumber String board serial number, mandatory
     * @param password String board password
     * @throws IllegalArgumentException if the serial number is blank
     */
    public Board(String name, @Nonnull String serialNumber, String password) throws IllegalArgumentException {

        if(StringUtils.isBlank(serialNumber)) throw new IllegalArgumentException("Serial number cannot be blank");

        this.name = name;
        this.serialNumber = serialNumber;
        this.password = password;
    }

    /**
     * Create a new board with a given name and serial number
     * @param name String board name
     * @param serialNumber String board serial number, mandatory
     * @throws IllegalArgumentException if the serial number is blank
     */
    public Board(String name, @Nonnull String serialNumber) {

        if(StringUtils.isBlank(serialNumber)) throw new IllegalArgumentException("Serial number cannot be blank");

        this.name = name;
        this.serialNumber = serialNumber;
    }

    /**
     * Create a new board with a given serial number
     * @param serialNumber String board serial number, mandatory
     * @throws IllegalArgumentException if the serial number is blank
     */
    public Board(@Nonnull String serialNumber) {

        if(StringUtils.isBlank(serialNumber)) throw new IllegalArgumentException("Serial number cannot be blank");

        this.serialNumber = serialNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return serialNumber.equals(board.serialNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serialNumber);
    }
}
