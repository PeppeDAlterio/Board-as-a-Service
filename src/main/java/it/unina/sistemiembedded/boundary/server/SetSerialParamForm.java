package it.unina.sistemiembedded.boundary.server;

import com.fazecast.jSerialComm.SerialPort;
import it.unina.sistemiembedded.model.Board;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SetSerialParamForm extends JFrame {
    private JTextField textFieldBaudRate;
    private JTextField textFieldNumData;
    private JTextField textFieldStopBit;
    private JButton applyButton;
    private JPanel mainPanel;
    private JTextField textFieldParity;
    private JComboBox comboBoxParity ;
    private JComboBox comboBoxStop;
    private JComboBox comboBoxData;
    private JComboBox comboBoxBoudRate;

    private final Logger logger = LoggerFactory.getLogger(SetSerialParamForm.class);

    private String[] boudRateValues = {"9600","115200","38400","19200","4800","2400"};
    private String[] numDataBitValues = {"8","5","6","7","9"};
    private String[] numStopBitValues = {"1","2"};
    private String[] parityValues = {"None","Odd","Even"};
    private int boudRate;
    private int bitData;
    private int bitStop;
    private String parity;

    private void initComboBox(){
        for (int i=0;i<boudRateValues.length;i++){
            comboBoxBoudRate.addItem(boudRateValues[i]);
        }
        for (int i=0;i<numDataBitValues.length;i++){
            comboBoxData.addItem(numDataBitValues[i]);
        }
        for (int i=0;i<numStopBitValues.length;i++){
            comboBoxStop.addItem(numStopBitValues[i]);
        }
        for (int i=0;i<parityValues.length;i++){
            comboBoxParity.addItem(parityValues[i]);
        }

    }

    public SetSerialParamForm(JFrame parent, Board board){

        parent.setEnabled(false);

        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setTitle(board+" serial parameters");
        initComboBox();

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                boudRate = Integer.parseInt(comboBoxBoudRate.getSelectedItem().toString());
                bitData = Integer.parseInt(comboBoxData.getSelectedItem().toString());
                bitStop = Integer.parseInt(comboBoxStop.getSelectedItem().toString());
                parity = comboBoxParity.getSelectedItem().toString();
                if (board.getComDriver().isPresent()) {
                    board.getComDriver().get().getSerialPort().setBaudRate(boudRate);
                    board.getComDriver().get().getSerialPort().setNumDataBits(bitData);
                    board.getComDriver().get().getSerialPort().setNumStopBits(bitStop);
                    if (parity.compareTo("None") == 0) {
                        board.getComDriver().get().getSerialPort().setParity(SerialPort.NO_PARITY);
                    }else if(parity.compareTo("Even")==0) {
                        board.getComDriver().get().getSerialPort().setParity(SerialPort.EVEN_PARITY);
                    }else{
                        board.getComDriver().get().getSerialPort().setParity(SerialPort.ODD_PARITY);
                    }
                }
                logger.info("Params set to : BoudRate : "+boudRate+" bitData : "+bitData+" bitStop : "+bitStop+" parity : "+parity);
            }
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                parent.setEnabled(true);
                parent.requestFocus();
            }
        });

    }




}
