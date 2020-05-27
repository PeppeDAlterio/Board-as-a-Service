package it.unina.sistemiembedded.driver;

import com.fazecast.jSerialComm.SerialPort;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class COMPort {

    public static List<COMPort> listCOMPorts() {

        SerialPort[] serialPortList = SerialPort.getCommPorts();

        ArrayList<COMPort> list = new ArrayList<>(serialPortList.length);

        for (SerialPort port : serialPortList) {
            list.add(new COMPort(port));
        }


        return Collections.unmodifiableList(list);

    }

    private final SerialPort delegate;

    public COMPort(SerialPort delegate) {
        this.delegate = delegate;
    }

    @Override
    public String toString() {
        if(delegate != null) {
            return delegate.getDescriptivePortName();
        } else {
            return "";
        }
    }

    public @Nullable SerialPort getSerialPort() {
        return this.delegate;
    }

}
