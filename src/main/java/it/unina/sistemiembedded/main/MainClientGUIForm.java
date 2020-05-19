package it.unina.sistemiembedded.main;

import it.unina.sistemiembedded.boundary.client.ChoiseForm;
import it.unina.sistemiembedded.client.impl.ClientImpl;
import it.unina.sistemiembedded.model.Board;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

@Getter @Setter
public class MainClientGUIForm extends  JFrame{
    private JPanel mainPanel;
    private JButton buttonStartConnection;
    private JTextField textFieldName;
    private JList listBoard;
    private JList listLab;
    private JButton refreshButtonLab;
    private JButton refreshButtonBoard;

    private DefaultListModel modelLab = new DefaultListModel();
    private DefaultListModel modelBoard = new DefaultListModel();

    private String name;
    private final String ipAddress="localhost";
    private ClientImpl client;

    //SCOPE : avoid errors if the user doesn't select any items in the list
    private void InitList(JList listLab , JList listBoard){
        listLab.setSelectedIndex(0);
        listBoard.setModel(modelBoard);
        Lab lab_selected = (Lab) listLab.getSelectedValue();
        for(int i=0 ;i<lab_selected.getBoard().size();i++)
            modelBoard.addElement(lab_selected.getBoard().get(i));
        listBoard.setSelectedIndex(0);
    }


    public MainClientGUIForm() throws IOException {
        this.setContentPane(this.mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setTitle("Lab as a Service application Client");
        listLab.setModel(modelLab);
        modelLab.addElement(new Lab());
        InitList(listLab,listBoard);
        String default_name = "Client"+ Math.random()*100;
        client = new ClientImpl(default_name);
        client.connect(ipAddress);
        listLab.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                modelBoard.clear();
                Lab lab_selected = (Lab) listLab.getSelectedValue();
                for(int i=0 ;i<lab_selected.getBoard().size();i++)
                    modelBoard.addElement(lab_selected.getBoard().get(i));
            }
        });
        buttonStartConnection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                name = textFieldName.getText();
                if(name.compareTo("")==0){
                    JOptionPane.showMessageDialog(null,"You'll start with name: "+default_name,"Field name is blank",JOptionPane.WARNING_MESSAGE);
                }else {
                    client.setName(name);
                }
                Board selectedBoard = (Board) listBoard.getSelectedValue();
                client.requestBoard(selectedBoard.getId());
                new ChoiseForm(client, listLab.getSelectedValue().toString(), listBoard.getSelectedValue().toString());
                dispose();
            }
        });

        refreshButtonLab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO : Refresh Lab list
            }
        });
        refreshButtonBoard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO : Refresh board list for each lab

            }
        });
    }

    public static void main(String[] args) throws IOException {
        new MainClientGUIForm();
    }
    //STUB

    public class Lab{
        private String exampleLab[] = {"Claudio LAB1"};
        private Board exampleBoard[] = {new Board("0","STM32F4x","XXXX",null,"1234"),
                new Board("1","STM32F3x","YYYY",null,"1234")};

        private String name;
        private ArrayList<Board> board = new ArrayList<Board>();

        public String getName() {
            return name;
        }

        public ArrayList<Board> getBoard() {
            return board;
        }

        public Lab(){
                name = exampleLab[0];
                board.add(exampleBoard[0]);
                board.add(exampleBoard[1]);
        }
        @Override
        public String toString() {
            return "Lab{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

}
