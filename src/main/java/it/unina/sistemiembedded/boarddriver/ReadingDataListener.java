package it.unina.sistemiembedded.boarddriver;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.util.List;


class ReadingDataListener implements SerialPortDataListener {

    private String buffer = "";

    private SerialPort comPort;

    private List<String> messages;

    private OutputBuffer outputBuffer;

    public ReadingDataListener(SerialPort comPort, List<String> messages, OutputBuffer outputBuffer) {

        this.comPort = comPort;

        if (!comPort.isOpen()) {
            throw new IllegalArgumentException();
        }

        this.messages = messages;

        this.outputBuffer = outputBuffer;

    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_DATA_WRITTEN;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {

        if (serialPortEvent.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {

            byte[] readData = new byte[comPort.bytesAvailable()];
            comPort.readBytes(readData, readData.length);

            this.buffer = this.buffer.concat(new String(readData));

            if (this.buffer.endsWith("\n") || this.buffer.endsWith("\r")) {
                buffer = buffer.replace("\n", "").replace("\r", "");
                messages.add(buffer);
                buffer = "";
            }

        } else if(serialPortEvent.getEventType() == SerialPort.LISTENING_EVENT_DATA_WRITTEN) {
            this.outputBuffer.setBusy(false);
        }

    }

}
