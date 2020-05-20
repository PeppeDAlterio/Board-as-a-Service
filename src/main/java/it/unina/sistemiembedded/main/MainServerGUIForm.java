package it.unina.sistemiembedded.main;

import it.unina.sistemiembedded.boundary.server.ServerStartedForm;
import it.unina.sistemiembedded.boundary.server.SetSerialParamForm;
import it.unina.sistemiembedded.exception.BoardAlreadyExistsException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.server.impl.ServerImpl;
import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MainServerGUIForm extends JFrame {

    private JPanel mainPanel;
    private JTextField textFieldName;
    private JList<Object> listBoard;
    private JButton startServerButton;
    private JButton buttonRefresh;


    private String nameServer = "Server-"+((int) (Math.random()*1000+1000));
    private Server server;

    private void initGUI(){
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setTitle("Board as a Service Server Application");
        this.textFieldName.setText(nameServer);
    }

    private void setSize(double height_inc,double weight_inc){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height *height_inc);
        int width = (int) (screenSize.width *weight_inc);
        this.setPreferredSize(new Dimension(width, height));
    }

    private void initList(){
        DefaultListModel<Object> defaultListModelBoard = new DefaultListModel<>();
        List<Board> boardList = server.rebuildBoards();
        if(boardList.size()!=0) {
            for (Board board : boardList) {
                defaultListModelBoard.addElement(board);
            }
        } else {
            defaultListModelBoard.addElement("No boards detected");
        }
        listBoard.setModel(defaultListModelBoard);
    }

    public MainServerGUIForm() {
        super();
        setSize(0.5,0.5);
        initGUI();
        server = new ServerImpl(nameServer);
        initList();
        listBoard.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting() && listBoard.getSelectedValue()!=null && listBoard.getSelectedValue() instanceof Board) {
                Board board = (Board) listBoard.getSelectedValue();
                new SetSerialParamForm(this, board);
                listBoard.clearSelection();
            }
        });

        startServerButton.addActionListener(new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = textFieldName.getText();
                if(name.compareTo("")==0){
                    name = nameServer;
                }
                server.setName(name);
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

        textFieldName.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                textFieldName.setText("");
            }
        });
    }

    public static void main(String[] args) throws BoardAlreadyExistsException {
        new MainServerGUIForm();
    }
}
