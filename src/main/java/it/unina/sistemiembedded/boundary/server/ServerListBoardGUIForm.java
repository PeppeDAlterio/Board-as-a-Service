package it.unina.sistemiembedded.boundary.server;

import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.utility.ui.UILongRunningHelper;
import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ServerListBoardGUIForm extends JFrame {

    private JPanel mainPanel;
    private JList<Object> listBoard;
    private JButton startServerButton;
    private JButton buttonRefresh;
    private Server server;


    private void initGUI(){
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Server - Board as a Service");
    }

    private void setSize(double height_inc,double weight_inc){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height *height_inc);
        int width = (int) (screenSize.width *weight_inc);
        this.setPreferredSize(new Dimension(width, height));
    }

    private void initList() throws InterruptedException {
        DefaultListModel<Object> defaultListModelBoard = new DefaultListModel<>();

        UILongRunningHelper.runAsync(this, "Messaggio di esempio di attesa...", () -> {

            List<Board> boardList = server.rebuildBoards();
            if(boardList.size()!=0) {
                for (Board board : boardList) {
                    defaultListModelBoard.addElement(board);
                }
            } else {
                defaultListModelBoard.addElement("No boards detected");
            }
            listBoard.setModel(defaultListModelBoard);

        });

    }

    public ServerListBoardGUIForm(Server server) {
        super();
        this.server=server;
        setSize(0.5,0.5);
        initGUI();
        try {
            initList();
        } catch (InterruptedException ignored) { }
        listBoard.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting() && listBoard.getSelectedValue()!=null && listBoard.getSelectedValue() instanceof Board) {
                Board board = (Board) listBoard.getSelectedValue();
                new SetSerialParamForm(this, server,board);
                listBoard.clearSelection();
            }
        });

        startServerButton.addActionListener(new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                server.start();
                new ServerStartedForm(server);
            }
        });
        buttonRefresh.addActionListener(new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                initList();
            }
        });

    }
}
