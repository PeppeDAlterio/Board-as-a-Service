package it.unina.sistemiembedded.boundary.client;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MainClientGUIForm extends  JFrame{
    private JPanel mainPanel;
    private JButton buttonStartConnection;
    private JTextField textFieldName;
    private JTextField textFieldSurname;
    private JList listBoard;
    private JList listLab;
    private JButton refreshButtonLab;
    private JButton refreshButtonBoard;
    private DefaultListModel modelLab = new DefaultListModel();
    private DefaultListModel modelBoard = new DefaultListModel();
    private Lab lab1;
    private Lab lab2;


    //SCOPE : avoid errors if the user doesn't select any items in the list
    private void InitList(JList listLab , JList listBoard){
        listLab.setSelectedIndex(0);
        listBoard.setModel(modelBoard);
        Lab lab_selected = (Lab) listLab.getSelectedValue();
        for(int i=0 ;i<lab_selected.getBoard().size();i++)
            modelBoard.addElement(lab_selected.getBoard().get(i).toString());
        listBoard.setSelectedIndex(0);
    }
    public MainClientGUIForm() {
        this.setContentPane(this.mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setTitle("Lab as a Service application Client");
        listLab.setModel(modelLab);
        modelLab.addElement(new Lab(0));
        modelLab.addElement(new Lab(1));
        InitList(listLab,listBoard);
        listLab.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                modelBoard.clear();
                Lab lab_selected = (Lab) listLab.getSelectedValue();
                for(int i=0 ;i<lab_selected.getBoard().size();i++)
                    modelBoard.addElement(lab_selected.getBoard().get(i).toString());
            }
        });
        buttonStartConnection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = textFieldName.getText();
                String surname = textFieldSurname.getText();
                if(name.compareTo("")==0 | surname.compareTo("")==0){
                    JOptionPane.showMessageDialog(null,"name or surname can not be blank !","Error message",JOptionPane.ERROR_MESSAGE);
                }else
                    new ChoiseForm(name,surname,listLab.getSelectedValue().toString(),listBoard.getSelectedValue().toString());
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

    public static void main(String[] args) {
        MainClientGUIForm mainClientGUIForm = new MainClientGUIForm();
    }

    //STUB
    public class Lab{
        private String exampleLab[] = {"Claudio LAB1","Agnano LAB3"};
        private String exampleBoard[] = {"STM32F4x","STM32F3x","STM32F4Discovery"};
        private String name;
        private ArrayList<String> board = new ArrayList<String>();

        public String getName() {
            return name;
        }

        public ArrayList<String> getBoard() {
            return board;
        }

        public Lab(int id){
            if(id==1) {
                name = exampleLab[0];
                board.add(exampleBoard[0]);
                board.add(exampleBoard[1]);
                board.add(exampleBoard[2]);
            }else if(id == 0){
                name = exampleLab[1];
                board.add(exampleBoard[0]);
                board.add(exampleBoard[1]);
            }
        }

        @Override
        public String toString() {
            return "Lab{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }
}
