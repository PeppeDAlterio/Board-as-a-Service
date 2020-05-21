package it.unina.sistemiembedded.boundary.server;

import it.unina.sistemiembedded.driver.COMDriver;
import it.unina.sistemiembedded.driver.COMPort;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.Server;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

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
    private JComboBox comboBoxlistComPort;
    private JComboBox comboBoxFlowControl;
    private JButton buttonInformation;


    private final Logger logger = LoggerFactory.getLogger(SetSerialParamForm.class);

    private String[] boudRateValues = {"9600","115200","38400","19200","4800","2400"};
    private String[] numDataBitValues = {"8","5","6","7","9"};
    private String[] numStopBitValues = {"1","2"};
    private String[] parityValues = {"N0_PARITY","ODD_PARITY","EVEN_PARITY","MARK_PARITY","SPACE_PARITY"};
    private String[] flowControlValues = {"FLOW_CONTROL_XONXOFF_IN_ENABLED","FLOW_CONTROL_XONXOFF_OUT_ENABLED","FLOW_CONTROL_DISABLED","FLOW_CONTROL_CTS_ENABLED",
                                            "FLOW_CONTROL_DSR_ENABLED","FLOW_CONTROL_DTR_ENABLED","FLOW_CONTROL_RTS_ENABLED"};
    private List<COMPort> listComPort = COMPort.listCOMPorts();

    private int boudRate;
    private int bitData;
    private int bitStop;
    private String parity;
    private String flowControl;
    private COMPort comPort;

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
        for(int i=0;i<listComPort.size();i++){
            comboBoxlistComPort.addItem(listComPort.get(i));
        }
        for (int i=0;i<flowControlValues.length;i++){
            comboBoxFlowControl.addItem(flowControlValues[i]);
        }
    }

    private void setSize(double height_inc,double weight_inc){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height *height_inc);
        int width = (int) (screenSize.width *weight_inc);
        this.setPreferredSize(new Dimension(width, height));
    }

    public SetSerialParamForm(JFrame parent, Server server ,Board board){
        super();
        parent.setEnabled(false);
        setSize(0.5,0.5);
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle(board+" serial parameters");

        initComboBox();

        applyButton.addActionListener(new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                boudRate = Integer.parseInt(comboBoxBoudRate.getSelectedItem().toString());
                bitData = Integer.parseInt(comboBoxData.getSelectedItem().toString());
                bitStop = Integer.parseInt(comboBoxStop.getSelectedItem().toString());
                comPort = (COMPort)comboBoxlistComPort.getSelectedItem();
                parity = comboBoxParity.getSelectedItem().toString();
                flowControl = comboBoxFlowControl.getSelectedItem().toString();
                //TODO : risolvere il bug che non permette di selezionare la stessa comPort sulla stessa scheda
                server.setBoardCOMDriver(board.getSerialNumber(),new COMDriver(comPort,boudRate,parity,bitData,bitStop,flowControl));
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


        buttonInformation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,"Before select a specific Com port:\n" +
                        "check the correspondence to the selected board in the section \"Ports(COM and LPT)\" of your management device settings","Important!",JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
}
