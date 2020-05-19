package it.unina.sistemiembedded.main;

import it.unina.sistemiembedded.boundary.server.ServerStartedForm;
import it.unina.sistemiembedded.boundary.server.SetSerialParamForm;
import it.unina.sistemiembedded.exception.BoardAlreadyExistsException;
import it.unina.sistemiembedded.model.Board;
import it.unina.sistemiembedded.server.impl.ServerImpl;
import it.unina.sistemiembedded.utility.SystemHelper;
import lombok.SneakyThrows;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MainServerGUIForm extends JFrame {

    private JPanel MainPanel;
    private JTextField textFieldName;
    private JList listBoard;
    private JButton startServerButton;
    private DefaultListModel defaultListModel;
    private Board board[];

    private ServerImpl server;

    private void addElementToList(ArrayList<Board> boardList){
        if(boardList.size()!=0) {
            for (int i = 0; i < server.listBoards().size(); i++)
                defaultListModel.addElement(boardList.get(i));
        }else{
            defaultListModel.addElement("No avaible boards");
        }

    }

    public MainServerGUIForm(ArrayList<Board> boardList) throws BoardAlreadyExistsException {
        super();
        this.setContentPane(MainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setTitle("Board as a Service Server Application");

        server = new ServerImpl("DEFAULT_NAME");
        server.addBoards(boardList.toArray(new Board[0]));
        defaultListModel = new DefaultListModel();
        addElementToList(boardList);
        listBoard.setModel(defaultListModel);


        listBoard.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(listBoard.getSelectedValue().getClass().toString().contains("Board")){
                    Board selectedBoard = (Board) listBoard.getSelectedValue();
                    new SetSerialParamForm(selectedBoard);
                }
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

        ArrayList<Board> boardList = (ArrayList<Board>) SystemHelper.listBoards();
        new MainServerGUIForm(boardList);

    }
}
