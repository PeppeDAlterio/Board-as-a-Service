package it.unina.sistemiembedded.driver;

import com.fazecast.jSerialComm.SerialPort;
import it.unina.sistemiembedded.server.ClientHandler;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class COMDriver {

    private static Logger logger = LoggerFactory.getLogger(COMDriver.class);

    // TODO: Documentazione
    @Getter
    private final SerialPort serialPort;

    // TODO: Documentazione
    private OutputBuffer outputBuffer = new OutputBuffer();

    /**
     * Handler of the Client holding the port
     */
    private ClientHandler clientHandler;

    // TODO: Documentazione
    private int br;
    private int P;
    private int db;
    private int sb;
    private int fc;

    /**
     * Return the int value of the passed string
     * 
     * @param string String
     * @return int integer parity value as SerialPort enum
     */
    private int parseParity(String string) {

        int id = 0;

        switch (string) {
            case "NO_PARITY":
                id = SerialPort.NO_PARITY;
                break;
            case "EVEN_PARITY":
                id = SerialPort.EVEN_PARITY;
                break;
            case "ODD_PARITY":
                id = SerialPort.ODD_PARITY;
                break;
        }

        return id;

    }

    /**
     * Parse flow control string collection
     * 
     * @param stringSet Collection string of flow controls
     * @return int flow control value as SerialPort enum
     */
    private int parseFlowControl(Collection<String> stringSet) {

        int flowControl = 0;

        for (String s : stringSet) {

            switch (s) {
                case "XONXOFF_IN_ENABLED":
                    flowControl |= SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED;
                    break;
                case "XONXOFF_OUT_ENABLED":
                    flowControl |= SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED;
                    break;
                case "DISABLED":
                    flowControl |= SerialPort.FLOW_CONTROL_DISABLED;
                    break;
                case "CTS_ENABLED":
                    flowControl |= SerialPort.FLOW_CONTROL_CTS_ENABLED;
                    break;
                case "DSR_ENABLED":
                    flowControl |= SerialPort.FLOW_CONTROL_DSR_ENABLED;
                    break;
                case "DTR_ENABLED":
                    flowControl |= SerialPort.FLOW_CONTROL_DTR_ENABLED;
                    break;
                case "RTS_ENABLED":
                    flowControl |= SerialPort.FLOW_CONTROL_RTS_ENABLED;
                    break;
            }

        }

        return flowControl;

    }

    //TODO: inserire documentazione
    public void changeParameters(int boudRate, String parity, int numBitData, int numBitStop,
            Collection<String> flowControl) {

        this.serialPort.setBaudRate(boudRate);
        this.serialPort.setNumDataBits(numBitData);
        this.serialPort.setNumStopBits(numBitStop);
        this.serialPort.setParity(parseParity(parity));
        this.serialPort.setFlowControl(parseFlowControl(flowControl));

    }

    //TODO: inserire documentazione
    public COMDriver(COMPort comPort, int boudRate, String parity, int numBitData, int numBitStop,
            Collection<String> flowControl) {

        this.serialPort = comPort.getSerialPort();
        if (this.serialPort == null || !this.serialPort.openPort()) {
            throw new IllegalArgumentException();
        }

        this.serialPort.setBaudRate(boudRate);
        this.serialPort.setNumDataBits(numBitData);
        this.serialPort.setNumStopBits(numBitStop);
        this.serialPort.setParity(parseParity(parity));
        this.serialPort.setFlowControl(parseFlowControl(flowControl));

        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 5000, 5000);

    }

    //TODO: inserire documentazione
    public void setClientHandler(ClientHandler clientHandler) {

        if(clientHandler==null) return;

        removeClientHandler();

        this.clientHandler = clientHandler;
        this.serialPort.addDataListener(new COMPortDataListener(clientHandler, outputBuffer));

    }

    public void removeClientHandler() {
        this.serialPort.removeDataListener();
        this.clientHandler = null;
    }

    //TODO: inserire documentazione
    public Optional<ClientHandler> getClientHandler() {
        return Optional.ofNullable(this.clientHandler);
    }

    //TODO: inserire documentazione
    public void closeCommunication() {
        if (serialPort != null) {
            serialPort.closePort();
        }
        this.clientHandler = null;
    }

    //TODO: inserire documentazione
    public void writeln(String str) {

        if (str == null || str.length() == 0)
            return;

        if (!str.endsWith("\r") || !str.endsWith("\n")) {
            str = str.concat("\r");
        }

        write(str);

    }

    //TODO: inserire documentazione
    public void write(String str) {

        if(str == null || str.length() == 0) return;

        final String finalStr = str;

        CompletableFuture.<Boolean>supplyAsync(() -> {

            int count = 0;
            while (this.outputBuffer.isBusy()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                } finally {
                    count++;
                }
                if (count == 20) {
                    return false;
                }
            }

            return true;

        }).thenAccept(result -> {
            if (result) {
                this.outputBuffer.setBusy(true);
                this.serialPort.writeBytes(finalStr.getBytes(), finalStr.getBytes().length);
            }
            else {
                logger.warn("[write] Timeout, output Buffer is busy " + this.serialPort);
            }
        });

    }

}
