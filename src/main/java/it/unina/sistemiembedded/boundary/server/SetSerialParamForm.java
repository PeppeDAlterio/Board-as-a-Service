package it.unina.sistemiembedded.boundary.server;

import com.fazecast.jSerialComm.SerialPort;
import it.unina.sistemiembedded.model.Board;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SetSerialParamForm extends JFrame {
    private JTextField textFieldBaudRate;
    private JTextField textFieldNumData;
    private JTextField textFieldStopBit;
    private JButton applyButton;
    private JPanel mainPanel;
    private JTextField textFieldParity;

    public SetSerialParamForm(Board board){
        System.out.println("SetSerialParamForm");
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setTitle(board.getName()+" serial parameters");
        textFieldBaudRate.setText("115200");
        textFieldNumData.setText("8");
        textFieldStopBit.setText("1");
        textFieldParity.setText("NO_PARITY");
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                int boudRate = Integer.parseInt(textFieldBaudRate.getText());
                int bitData = Integer.parseInt(textFieldNumData.getText());
                int bitStop = Integer.parseInt(textFieldStopBit.getText());
                String parity = textFieldParity.getText();
                if (board.getComDriver().isPresent()) {
                    board.getComDriver().get().getComPort().setBaudRate(boudRate);
                    board.getComDriver().get().getComPort().setNumDataBits(bitData);
                    board.getComDriver().get().getComPort().setNumStopBits(bitStop);
                    if (parity.compareTo("NO_PARITY") == 0)
                        board.getComDriver().get().getComPort().setParity(SerialPort.NO_PARITY);
                    else
                        board.getComDriver().get().getComPort().setParity(SerialPort.EVEN_PARITY);
                }
            }
        });
    }
}
