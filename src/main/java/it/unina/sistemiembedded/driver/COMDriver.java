package it.unina.sistemiembedded.driver;

import com.fazecast.jSerialComm.SerialPort;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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
     * @param string
     * @return id
     */
    private int parser(String string){

        int id = 0;

        if(string.contains("PARITY")){

            if(string.contains("NO")){
                id=SerialPort.NO_PARITY;
            }else if(string.contains("EVEN")){
                id=SerialPort.EVEN_PARITY;
            }else if(string.contains("ODD")){
                id=SerialPort.ODD_PARITY;
            }

        } else if(string.contains("FLOW_CONTROL")) {

            if(string.contains("XONXOFF_IN_ENABLED")){
                id |= SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED;
            }

            if(string.contains("XONXOFF_OUT_ENABLED")){
                id |= SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED;
            }

            if(string.contains("DISABLED")){
                id |= SerialPort.FLOW_CONTROL_DISABLED;
            }

            if(string.contains("CTS_ENABLED")){
                id |= SerialPort.FLOW_CONTROL_CTS_ENABLED;
            }

            if(string.contains("DSR_ENABLED")){
                id |= SerialPort.FLOW_CONTROL_DSR_ENABLED;
            }

            if(string.contains("DTR_ENABLED")){
                id |= SerialPort.FLOW_CONTROL_DTR_ENABLED;
            }

            if(string.contains("RTS_ENABLED")){
                id |= SerialPort.FLOW_CONTROL_RTS_ENABLED;
            }

        }

        return id;

    }

    //TODO: trovare un modo per creare un metodo pubblico
    public COMDriver(COMPort comPort, int boudRate, String parity, int numBitData, int numBitStop, String flowControl) {
        this.serialPort = comPort.getSerialPort();
        if(this.serialPort == null || !this.serialPort.openPort()) {
            throw new IllegalArgumentException();
        }

        this.serialPort.setBaudRate(boudRate);
        this.serialPort.setNumDataBits(numBitData);
        this.serialPort.setNumStopBits(numBitStop);
        this.serialPort.setParity(parser(parity));
        this.serialPort.setFlowControl(parser(flowControl));

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

    public void writeLn(String str) {

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
