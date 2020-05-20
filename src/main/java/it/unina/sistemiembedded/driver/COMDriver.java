package it.unina.sistemiembedded.driver;

import com.fazecast.jSerialComm.SerialPort;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter @Setter
public class COMDriver {

    private SerialPort comPort;

    private LinkedList<String> availableMessages = new LinkedList<>();

    private OutputBuffer outputBuffer = new OutputBuffer();

    private int br;
    private int P;
    private int db;
    private int sb;
    private int fc;

    //TODO: trovare un modo per creare un metodo pubblico
    public COMDriver(SerialPort serialPort, int br, int P, int db, int sb, int fc) {
        this.comPort = serialPort;
        if(this.comPort == null || !serialPort.openPort()) {
            throw new IllegalArgumentException();
        }

        // comPort.setBaudRate(115200);
        // comPort.setNumDataBits(8);
        // comPort.setNumStopBits(1);
        // comPort.setParity(SerialPort.NO_PARITY);
        // comPort.setFlowControl(SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED);

        serialPort.setBaudRate(br);
        serialPort.setNumDataBits(db);
        serialPort.setNumStopBits(sb);
        serialPort.setParity(P);
        serialPort.setFlowControl(fc);


        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 5000, 5000);

        serialPort.addDataListener(new ReadingDataListener(this.comPort, this.availableMessages, outputBuffer));

    }

    public List<String> consumeAllAvailableMessages() {
        List<String> copyOfMessage = new ArrayList<>(this.availableMessages);
        this.availableMessages.clear();
        return copyOfMessage;
    }

    public void closeCommunication() {
        if(comPort != null) {
            comPort.closePort();
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

            this.comPort.writeBytes(str.getBytes(), str.getBytes().length);

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

            this.comPort.writeBytes(str.getBytes(), str.getBytes().length);

        }

    }

}
