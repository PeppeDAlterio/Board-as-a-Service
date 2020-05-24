package it.unina.sistemiembedded.driver;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import it.unina.sistemiembedded.server.ClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class COMPortDataListener implements SerialPortDataListener {

    private static final Logger logger = LoggerFactory.getLogger(COMPortDataListener.class);

    private StringBuilder buffer = new StringBuilder();

    private final ClientHandler clientHandler;

    private OutputBuffer outputBuffer;

    // TODO documentazione
    public COMPortDataListener(ClientHandler clientHandler, OutputBuffer outputBuffer) {

        this.clientHandler = clientHandler;

        this.outputBuffer = outputBuffer;

    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_DATA_WRITTEN;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {

        if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {

            byte[] readData = new byte[event.getSerialPort().bytesAvailable()];
            event.getSerialPort().readBytes(readData, readData.length);

            String readString = new String(readData);

            this.buffer.append(readString);

            if (readString.endsWith("\n") || readString.endsWith("\r")) {
                String bufferedString = buffer.toString().replace("\n", "").replace("\r", "");
                if(clientHandler!=null) {
                    logger.info("[serialEvent] COM Message sent to '" + clientHandler.getName() + "': " + bufferedString);
                    clientHandler.sendTextMessage(bufferedString);
                }
                buffer.setLength(0);
            }

        } else if(event.getEventType() == SerialPort.LISTENING_EVENT_DATA_WRITTEN) {
            this.outputBuffer.setBusy(false);
        }

    }

}