package it.unina.sistemiembedded.model;

import it.unina.sistemiembedded.driver.COMDriver;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.shared.utils.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

@Getter @Setter
public class Board implements Serializable {

    public static String SERIALIZATION_SEPARATOR = "\\/";
    public static int SERIALIZATION_NUMBER_OF_FIELDS = 2;

    /**
     * Board symbolic name
     */
    private String name;

    /**
     * Global unique serial number
     */
    private final String serialNumber;

    private final COMDriver comDriver;

    /**
     * Board password. Used to authorize the connection
     */
    private String password;

    private boolean inUse = false;

    /**
     * Create a new board with a given name, serial number and password.
     * @param name String board name
     * @param serialNumber String board serial number, mandatory
     * @param comDriver
     * @param password String board password
     * @throws IllegalArgumentException if the serial number is blank
     */
    public Board(String name, @Nonnull String serialNumber, COMDriver comDriver, String password) throws IllegalArgumentException {
        this.comDriver = comDriver;

        if(StringUtils.isBlank(serialNumber)) throw new IllegalArgumentException("Serial number cannot be blank");

        this.name = name;
        this.serialNumber = serialNumber.replace(SERIALIZATION_SEPARATOR, " ");
        this.password = password;
    }

    /**
     * Create a new board with a given name and serial number
     * @param name String board name
     * @param serialNumber String board serial number, mandatory
     * @param comDriver COMDriver com port driver, nullable
     * @throws IllegalArgumentException if the serial number is blank
     */
    public Board(String name,
                 @Nonnull String serialNumber,
                 @Nullable COMDriver comDriver) {

        if(StringUtils.isBlank(serialNumber)) throw new IllegalArgumentException("Serial number cannot be blank");

        this.name = name;
        this.serialNumber = serialNumber.replace(SERIALIZATION_SEPARATOR, " ");
        this.comDriver = comDriver;
    }

    /**
     * Create a new board with a given serial number
     * @param serialNumber String board serial number, mandatory
     * @param comDriver COMDriver com port driver, nullable
     * @throws IllegalArgumentException if the serial number is blank
     */
    public Board(@Nonnull String serialNumber,
                 @Nullable COMDriver comDriver) {

        if(StringUtils.isBlank(serialNumber)) throw new IllegalArgumentException("Serial number cannot be blank");

        this.serialNumber = serialNumber;
        this.comDriver = comDriver;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Optional<COMDriver> getComDriver() {
        return Optional.ofNullable(comDriver);
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

    @Override
    public String toString() {
        return this.name + SERIALIZATION_SEPARATOR + this.serialNumber ;
    }
}
