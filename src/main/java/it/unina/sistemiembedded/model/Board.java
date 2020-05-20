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

    public static String SERIALIZATION_SEPARATOR = "§§§";
    public static int SERIALIZATION_NUMBER_OF_FIELDS = 2;

    //private String id/* = UUID.randomUUID().toString()*/;

    /**
     * Board symbolic name
     */
    private String name;

    /**
     * Global unique serial number
     */
    private final String serialNumber;

    private COMDriver comDriver;

    /**
     * Board password. Used to authorize the connection
     */
    private String password = "";

    private boolean inUse = false;

    private Process debuggingProcess;

    public static Board deserialize(@Nonnull String serializedBoard) {

        if(StringUtils.isBlank(serializedBoard)) {
            throw new IllegalArgumentException("Data cannot be blank");
        }

        return new Board(serializedBoard);

    }

    /**
     * Deserializing constructor
     * @param serializedBoard String serialized board
     */
    private Board(@Nonnull String serializedBoard) {

        String[] data = serializedBoard.split(SERIALIZATION_SEPARATOR);

        if(data.length != SERIALIZATION_NUMBER_OF_FIELDS) {
            throw new IllegalArgumentException("Data length must be equal to " + SERIALIZATION_NUMBER_OF_FIELDS +
                    ", but is " + data.length);
        }

        //this.id = "";
        this.name = data[0];
        this.serialNumber = data[1];

        this.comDriver = null;

    }

    /**
     * Create a new board with a given name, serial number and password.
     * @param name String board name
     * @param serialNumber String board serial number, mandatory
     * @param comDriver COMDriver serial com driver
     * @param password String board password
     * @throws IllegalArgumentException if the serial number is blank
     */
    public Board(@Nonnull String name,
                 @Nonnull String serialNumber,
                 @Nullable COMDriver comDriver,
                 @Nullable String password) throws IllegalArgumentException {

        if(StringUtils.isBlank(serialNumber)) throw new IllegalArgumentException("Serial number cannot be blank");

        this.name = name.replace(SERIALIZATION_SEPARATOR, " ");
        this.serialNumber = serialNumber.replace(SERIALIZATION_SEPARATOR, " ");
        this.comDriver = comDriver;
        if(!StringUtils.isBlank(password)) {
            this.password = password.replace(SERIALIZATION_SEPARATOR, " ");
        }

    }

    /**
     * Create a new board with a given name and serial number
     * @param name String board name
     * @param serialNumber String board serial number, mandatory
     * @throws IllegalArgumentException if the serial number is blank
     */
    public Board(@Nonnull String name,
                 @Nonnull String serialNumber) {

        if(StringUtils.isBlank(serialNumber)) throw new IllegalArgumentException("Serial number cannot be blank");

        this.name = name;
        this.serialNumber = serialNumber.replace(SERIALIZATION_SEPARATOR, " ");
        this.comDriver = null;

    }

    /**
     * Create a new board with a given name and serial number
     * @param name String board name
     * @param serialNumber String board serial number, mandatory
     * @param comDriver COMDriver com port driver, nullable
     * @throws IllegalArgumentException if the serial number is blank
     */
    public Board(@Nonnull String name,
                 @Nonnull String serialNumber,
                 @Nullable COMDriver comDriver) throws IllegalArgumentException {

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
                 @Nullable COMDriver comDriver) throws IllegalArgumentException {

        if(StringUtils.isBlank(serialNumber)) throw new IllegalArgumentException("Serial number cannot be blank");

        this.serialNumber = serialNumber;
        this.comDriver = comDriver;
    }

//    public String getId() {
//        return this.name + "#" + this.serialNumber;
//    }

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
        return serialNumber.equals(board.serialNumber) && name.equals(board.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, serialNumber);
    }

    public String serialize() {
        return  this.name + SERIALIZATION_SEPARATOR +
                this.serialNumber ;
    }

    @Override
    public String toString() {
        return "[  Name = " + this.name + "  |  Serial Number = " + this.serialNumber+"  ] \n";
    }
}
