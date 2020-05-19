package it.unina.sistemiembedded.main;

import it.unina.sistemiembedded.boundary.server.ServerStartedForm;
import it.unina.sistemiembedded.boundary.server.SetSerialParamForm;
import it.unina.sistemiembedded.exception.BoardAlreadyExistsException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.impl.ServerImpl;
import lombok.SneakyThrows;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

public class MainServerGUIForm extends JFrame {

    private JPanel MainPanel;
    private JTextField textFieldName;
    private JList listBoard;
    private JButton startServerButton;
    private DefaultListModel defaultListModel;
    private Board board[];

    private ServerImpl server;

    private Board[] getBoards(LinkedList<Board> boardList){
        Board[] board = new Board[boardList.size()];
        for(int i=0;i<boardList.size();i++)
            board[i] = boardList.get(i);
        return board;
    }
    
    public MainServerGUIForm(LinkedList<Board> boardList) throws BoardAlreadyExistsException {
        super();
        this.setContentPane(MainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setTitle("Board as a Service Server Application");
        //TODO : Inizializzare la lista delle comPort
        server = new ServerImpl("DEFAULT_NAME");
        server.addBoards(getBoards(boardList));
        defaultListModel = new DefaultListModel();
        for(int i=0;i<server.listBoards().size();i++) {
            defaultListModel.addElement(server.listBoards().get(i));
        }
        listBoard.setModel(defaultListModel);

        listBoard.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Board selectedBoard = (Board) listBoard.getSelectedValue();
                new SetSerialParamForm(selectedBoard,selectedBoard.getName());
            }
        });

        startServerButton.addActionListener(new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                String nameServer = textFieldName.getText();
                if(nameServer.compareTo("")==0)
                    nameServer = "DEFAULT_NAME_SERVER";
                server.start();
                dispose();
                new ServerStartedForm(server);
            }
        });
    }

    public static void main(String[] args) throws BoardAlreadyExistsException {

        //TODO  : Funzione che riconosce le board attive e torna una lista
        //      : Board {Serial Number , name , com port}
        //Per ora uso uno stub
        LinkedList<Board> boardList = new LinkedList<>(); //Uso una linkedlist poichè inserisco con tempo O(1)
        boardList.add(new Board("0","STM32F4x","XXXX",null,"1234"));
        boardList.add(new Board("1","STM32F3x","YYYY",null,"1234"));
        new MainServerGUIForm(boardList);
    }
}
