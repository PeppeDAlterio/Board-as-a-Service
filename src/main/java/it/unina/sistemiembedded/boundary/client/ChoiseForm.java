package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.utility.ui.UILongRunningHelper;
import it.unina.sistemiembedded.utility.ui.UISizeHelper;
import it.unina.sistemiembedded.utility.ui.stream.CustomOutputStream;
import it.unina.sistemiembedded.utility.ui.stream.UIPrinterHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;

public class ChoiseForm extends ClientJFrame {
    private JPanel mainPanel;
    private JLabel labelBoard;
    private JButton sendMessageButton;
    private JButton remoteFlashButton;
    private JButton remoteDegubButton;
    private JLabel label_name;
    private JButton requestAnotherBoardButton;
    private JLabel labelInfoServer;
    private JButton buttonResetBoard;

    private RemoteDebugForm debugFrame;
    private RemoteFlashForm flashFrame;
    private SendMessageForm msgFrame;

    private final ClientJFrame $this=this;

    private PrintStream printStream;

    private void setVisibleFrames(boolean enabled){
            debugFrame.setVisible(enabled);
            flashFrame.setVisible(enabled);
            msgFrame.setVisible(enabled);
    }

    public ChoiseForm(Client client, String board, String ip, int port, JFrame parent) {
        super("Board controller - Board as a Service");
        debugFrame = new RemoteDebugForm(client);
        flashFrame = new RemoteFlashForm(client);
        msgFrame = new SendMessageForm(client);

        printStream = new PrintStream(new CustomOutputStream(null, null, debugFrame.textAreaResponse, flashFrame.textAreaFlash, msgFrame.textAreaComunication));
        UIPrinterHelper.setPrintStream(printStream);

        setVisibleFrames(false);
        UISizeHelper.setSize(this,0.3,0.3);
        String infoLab = labelInfoServer.getText();
        infoLab = infoLab.replace("[IP]", "[ " + ip);
        infoLab = infoLab.replace("[PORT]",port + " ]");
        infoLab = infoLab.replace("[LAB]", client.getServerName());
        this.labelInfoServer.setText(infoLab);
        String infoBoard = labelBoard.getText();
        infoBoard = infoBoard.replace("[BOARD]",board);
        this.labelBoard.setText(infoBoard);
        String infoClient = label_name.getText();
        infoClient = infoClient.replace("[NAME]",client.getName());
        label_name.setText(infoClient);
        this.setContentPane(this.mainPanel);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setLocationRelativeTo(null);

        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                msgFrame.setVisible(true);
            }
        });
        remoteDegubButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                debugFrame.setVisible(true);
            }
        });
        remoteFlashButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flashFrame.setVisible(true);
            }
        });
        requestAnotherBoardButton.addActionListener(e -> {
                int choise=JOptionPane.showConfirmDialog($this,"You will be detached from the current board. Continue?","Request another board",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
                if(choise==JOptionPane.YES_OPTION) {
                    client.requestReleaseBoard();
                    dispose();
                    setVisibleFrames(false);
                    new AttachBoardForm(client, ip, port);
                }
        });


        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choise=JOptionPane.showConfirmDialog($this,"You will be detached from the current board. Continue?","Closing the current session...",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
                if(choise==JOptionPane.YES_OPTION) {
                    client.requestReleaseBoard();
                    setVisibleFrames(false);
                    dispose();
                    new AttachBoardForm(client, ip, port);
                }
            }
        });
        buttonResetBoard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UILongRunningHelper.runAsync($this,"Reset in progress...",()->{
                    client.requestBlockingReset();
                });
            }
        });
    }

}
