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

public class MainServerGUIForm extends JFrame {

    //Example Board---------------------------------------------------------------
    Board board1 = new Board("STM32F4x","xxxx");
    Board board2 = new Board("STM32F3x","yyyy");
    //----------------------------------------------------------------------------

    private JPanel MainPanel;
    private JTextField textFieldName;
    private JList listBoard;
    private JButton startServerButton;
    private DefaultListModel defaultListModel;

    private ServerImpl server;

    public MainServerGUIForm() throws BoardAlreadyExistsException {
        super();
        this.setContentPane(MainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setTitle("Board as a Service Server Application");
        //TODO : Inizializzare la lista delle comPort
        server = new ServerImpl("DEFAULT_NAME");
        server.addBoards(board1,board2);
        defaultListModel = new DefaultListModel();
        for(int i=0;i<server.listBoards().size();i++) {
            defaultListModel.addElement(server.listBoards().get(i));
        }
        listBoard.setModel(defaultListModel);

        listBoard.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Board selectedBoard = (Board) listBoard.getSelectedValue();
                new SetSerialParamForm(selectedBoard,board1.getName());
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
        new MainServerGUIForm();
    }
}
