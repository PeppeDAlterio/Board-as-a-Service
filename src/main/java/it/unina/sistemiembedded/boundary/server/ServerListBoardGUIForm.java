package it.unina.sistemiembedded.boundary.server;

import it.unina.sistemiembedded.main.MainServerGUIForm;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.ui.UILongRunningHelper;
import it.unina.sistemiembedded.utility.ui.UISizeHelper;
import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.BindException;
import java.util.List;

public class ServerListBoardGUIForm extends JFrame {

    private JPanel mainPanel;
    private JList<Object> listBoard;
    private JButton startServerButton;
    private JButton buttonRefresh;
    private JLabel labelport;
    private JLabel labelServerStarted;
    private Server server;

    private JFrame serverStartedForm;

    private JFrame $this = this;

    private int startedFirstTime =0;

    private void initGUI() {
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Server - Board as a Service");
        this.labelport.setText(this.labelport.getText().replace("#PORT",Integer.toString(server.getPort())));
        serverStartedForm = new ServerStartedForm(server,this);
        serverStartedForm.setVisible(false);
        labelServerStarted.setText(labelServerStarted.getText().replace("#PORT",Integer.toString(server.getPort())));
        labelServerStarted.setVisible(false);
    }


    private void initList() throws InterruptedException {
        DefaultListModel<Object> defaultListModelBoard = new DefaultListModel<>();
        UILongRunningHelper.runAsync(this, "Loading board's list...", () -> {
            List<Board> boardList = server.rebuildBoards();
            if (boardList.size() != 0) {
                for (Board board : boardList) {
                    defaultListModelBoard.addElement(board);
                }
            } else {
                defaultListModelBoard.addElement("No boards detected");
            }
            listBoard.setModel(defaultListModelBoard);
            listBoard.setVisibleRowCount(JList.VERTICAL_WRAP);
        });

    }

    public ServerListBoardGUIForm(Server server) {
        super("Server - Server Board's list Form");
        this.server = server;
        UISizeHelper.setSize(this,0.5, 0.5);
        initGUI();
        try {
            initList();
        } catch (InterruptedException ignored) {
        }
        listBoard.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listBoard.getSelectedValue() != null && listBoard.getSelectedValue() instanceof Board) {
                Board board = (Board) listBoard.getSelectedValue();
                new SetSerialParamForm(this, server, board);
                listBoard.clearSelection();
            }
        });

        startServerButton.addActionListener(e -> {
            if(startedFirstTime==0) {
                UILongRunningHelper.supplyAsync(this, "Startin server...", () -> {
                    try {
                        server.start();
                        startedFirstTime = 1;
                    } catch (BindException ex) {
                        return ex;
                    } catch (IOException ex) {
                        return ex;
                    }
                    return null;
                }, result -> {
                    if (result instanceof BindException) {
                        JOptionPane.showMessageDialog(this, "There is already an active connection to the specified port.", "Error!", JOptionPane.ERROR_MESSAGE);
                        dispose();
                        new MainServerGUIForm();
                    } else if (result instanceof IOException) {
                        JOptionPane.showMessageDialog(this, "Can't start server.", "Error!", JOptionPane.ERROR_MESSAGE);
                        new MainServerGUIForm();
                    } else {
                        this.setVisible(false);
                        serverStartedForm.setVisible(true);
                        startServerButton.setText("Open server console");
                        labelServerStarted.setVisible(true);
                    }
                });
            }else{
                this.setVisible(false);
                serverStartedForm.setVisible(true);
            }
        });
        buttonRefresh.addActionListener(new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                initList();
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //TODO : JOptionPane per segnalare termine sessione di debug(o eventualmente annullare)
                int choise=JOptionPane.showConfirmDialog($this,"This will shoutdown the server! Continue?","Stop server.",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
                if(choise==JOptionPane.YES_OPTION) {
                    dispose();
                    new MainServerGUIForm();
                }
            }
        });
    }

}
