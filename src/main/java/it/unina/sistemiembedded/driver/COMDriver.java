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

    //TODO: trovare un modo per creare un metodo pubblico
    public COMDriver(COMPort comPort, int br, int P, int db, int sb, int fc) {
        this.serialPort = comPort.getSerialPort();
        if(this.serialPort == null || !this.serialPort.openPort()) {
            throw new IllegalArgumentException();
        }

        // this.serialPort.setBaudRate(115200);
        // this.serialPort.setNumDataBits(8);
        // this.serialPort.setNumStopBits(1);
        // this.serialPort.setParity(SerialPort.NO_PARITY);
        // this.serialPort.setFlowControl(SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED);

        this.serialPort.setBaudRate(br);
        this.serialPort.setNumDataBits(db);
        this.serialPort.setNumStopBits(sb);
        this.serialPort.setParity(P);
        this.serialPort.setFlowControl(fc);


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
