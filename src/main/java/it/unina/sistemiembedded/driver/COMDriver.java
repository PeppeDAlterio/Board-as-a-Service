package it.unina.sistemiembedded.driver;

import com.fazecast.jSerialComm.SerialPort;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Getter @Setter
public class COMDriver {

    private SerialPort serialPort;

    private LinkedList<String> availableMessages = new LinkedList<>();

    private OutputBuffer outputBuffer = new OutputBuffer();

    private int br;
    private int P;
    private int db;
    private int sb;
    private int fc;

    /**
     * Return the int value of the passed string
     * @param string String
     * @return int integer parity value as SerialPort enum
     */
    private int parseParity(String string){

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

    //TODO: trovare un modo per creare un metodo pubblico
    public COMDriver(COMPort comPort, int boudRate, String parity, int numBitData, int numBitStop, Collection<String> flowControl) {

        this.serialPort = comPort.getSerialPort();
        if(this.serialPort == null || !this.serialPort.openPort()) {
            throw new IllegalArgumentException();
        }

        this.serialPort.setBaudRate(boudRate);
        this.serialPort.setNumDataBits(numBitData);
        this.serialPort.setNumStopBits(numBitStop);
        this.serialPort.setParity(parseParity(parity));
        this.serialPort.setFlowControl(parseFlowControl(flowControl));

        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 5000, 5000);

        this.serialPort.addDataListener(new ReadingDataListener(this.serialPort, this.availableMessages, outputBuffer));

    }

    public List<String> consumeAllAvailableMessages() {
        List<String> copyOfMessage = new ArrayList<>(this.availableMessages);
        this.availableMessages.clear();
        return copyOfMessage;
    }

    public void closeCommunication() {
        if(serialPort != null) {
            serialPort.closePort();
        }
    }

    public void writeln(String str) {

        if(str == null || str.length() == 0) return;

        if(!str.endsWith("\r") || !str.endsWith("\n")) {
            str = str.concat("\r");
        }

        synchronized (this) {

            while(this.outputBuffer.isBusy()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //FIXME: Aggiungi un controllo tc. dopo un timeout restituisci errore per evitare loop infiniti
            }


            this.outputBuffer.setBusy(true);

            this.serialPort.writeBytes(str.getBytes(), str.getBytes().length);

        }

    }

    public void write(String str) {

        if(str == null || str.length() == 0) return;

        synchronized (this) {

            while(this.outputBuffer.isBusy()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //FIXME: Aggiungi un controllo tc. dopo un timeout restituisci errore per evitare loop infiniti
            }


            this.outputBuffer.setBusy(true);

            this.serialPort.writeBytes(str.getBytes(), str.getBytes().length);

        }

    }

}
