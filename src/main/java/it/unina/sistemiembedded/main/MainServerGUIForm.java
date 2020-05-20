package it.unina.sistemiembedded.main;

import it.unina.sistemiembedded.boundary.server.ServerStartedForm;
import it.unina.sistemiembedded.boundary.server.SetSerialParamForm;
import it.unina.sistemiembedded.exception.BoardAlreadyExistsException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.server.impl.ServerImpl;
import it.unina.sistemiembedded.utility.SystemHelper;
import lombok.SneakyThrows;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class MainServerGUIForm extends JFrame {

    private JPanel MainPanel;
    private JTextField textFieldName;
    private JList listBoard;
    private JButton startServerButton;
    private JButton buttonRefresh;


    private List<Board> boardList;
    private String nameServer="Server-"+((int) (Math.random()*1000+1000));
    private Server server;

    private void initGUI(){
        this.setContentPane(MainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setTitle("Board as a Service Server Application");
        this.textFieldName.setText(nameServer);
    }


    private void initList() throws BoardAlreadyExistsException {
        boardList =  SystemHelper.listBoards();
        DefaultListModel defaultListModelBoard = new DefaultListModel();
        if(boardList.size()!=0){
            server.addBoards(boardList.toArray(new Board[0]));
            for(int i=0;i<boardList.size();i++)
                defaultListModelBoard.addElement(boardList.get(i));
        }else
            defaultListModelBoard.addElement("No boards detected");
        listBoard.setModel(defaultListModelBoard);
    }

    public MainServerGUIForm() throws BoardAlreadyExistsException {
        super();
        initGUI();
        server = new ServerImpl(nameServer);
        initList();
        listBoard.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(listBoard.getSelectedValue().getClass().toString().contains("Board")){
                    Board board = (Board) listBoard.getSelectedValue();
                    new SetSerialParamForm(board);
                }
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
                server.removeBoards(boardList);
                initList();
            }
        });
    }

    public static void main(String[] args) throws BoardAlreadyExistsException {
        new MainServerGUIForm();
    }
}
