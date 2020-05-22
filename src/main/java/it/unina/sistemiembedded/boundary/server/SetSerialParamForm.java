package it.unina.sistemiembedded.boundary.server;

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
import java.util.HashSet;
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
    private JButton buttonInformation;
    private JLabel labelFlowControl;
    private JRadioButton XONXOFF_IN_ENABLEDRadioButton;
    private JRadioButton XONXOFF_OUT_ENABLEDRadioButton;
    private JRadioButton DISABLEDRadioButton;
    private JRadioButton CTS_ENABLEDRadioButton;
    private JRadioButton DSR_ENABLEDRadioButton;
    private JRadioButton DTR_ENABLEDRadioButton;
    private JRadioButton RTS_ENABLEDRadioButton;



    private final Logger logger = LoggerFactory.getLogger(SetSerialParamForm.class);

    private String[] boudRateValues = {"9600","115200","38400","19200","4800","2400"};
    private String[] numDataBitValues = {"8","5","6","7","9"};
    private String[] numStopBitValues = {"1","2"};
    private String[] parityValues = {"N0_PARITY","ODD_PARITY","EVEN_PARITY","MARK_PARITY","SPACE_PARITY"};
    private List<COMPort> listComPort = COMPort.listCOMPorts();

    private int boudRate;
    private int bitData;
    private int bitStop;
    private String parity;
    private HashSet<String> flowControl = new HashSet<>();
    private COMPort comPort;

    private int index1;
    private int index2 = 0;
    private int index3 = 0;
    private int index4 = 0;
    private int index5 = 0;
    private int index6 = 0;
    private int index7 = 0;

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
        index1 = 2;

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
                //flowControl = comboBoxFlowControl.getSelectedItem().toString();
                //TODO : risolvere il bug che non permette di selezionare la stessa comPort sulla stessa scheda
                server.setBoardCOMDriver(board.getSerialNumber(),comPort,boudRate,bitData,bitStop,parity,flowControl);
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
        XONXOFF_IN_ENABLEDRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if((index1%2)==0){
                    flowControl.add(XONXOFF_IN_ENABLEDRadioButton.getText());
                }else{
                    flowControl.remove(XONXOFF_IN_ENABLEDRadioButton.getText());
                }
                index1++;
                System.out.println(flowControl);
            }
        });
        XONXOFF_OUT_ENABLEDRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if((index2%2)==0){
                    flowControl.add(XONXOFF_OUT_ENABLEDRadioButton.getText());
                }else{
                    flowControl.remove(XONXOFF_OUT_ENABLEDRadioButton.getText());
                }
                index2++;
                System.out.println(flowControl);
            }
        });
        DISABLEDRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if((index3%2)==0){
                    flowControl.add(DISABLEDRadioButton.getText());
                }else{
                    flowControl.remove(DISABLEDRadioButton.getText());
                }
                index3++;
                System.out.println(flowControl);
            }
        });
        CTS_ENABLEDRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if((index4%2)==0){
                    flowControl.add(CTS_ENABLEDRadioButton.getText());
                }else{
                    flowControl.remove(CTS_ENABLEDRadioButton.getText());
                }
                index4++;
                System.out.println(flowControl);
            }
        });
        DSR_ENABLEDRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if((index5%2)==0){
                    flowControl.add(DSR_ENABLEDRadioButton.getText());
                }else{
                    flowControl.remove(DSR_ENABLEDRadioButton.getText());
                }
                index5++;
                System.out.println(flowControl);
            }
        });
        DTR_ENABLEDRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if((index6%2)==0){
                    flowControl.add(DTR_ENABLEDRadioButton.getText());
                }else{
                    flowControl.remove(DTR_ENABLEDRadioButton.getText());
                }
                index6++;
                System.out.println(flowControl);
            }
        });
        RTS_ENABLEDRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if((index7%2)==0){
                    flowControl.add(RTS_ENABLEDRadioButton.getText());
                }else{
                    flowControl.remove(RTS_ENABLEDRadioButton.getText());
                }
                index7++;
                System.out.println(flowControl);
            }
        });
    }
}
