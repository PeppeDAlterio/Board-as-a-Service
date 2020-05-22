package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.model.Board;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

@Getter
@Setter
public class AttachBoardForm extends JFrame {
    private JPanel mainPanel;
    private JButton buttonRequestSelectedBoard;
    private JList listBoard;
    private JList listLab;
    private JButton refreshButtonBoard;
    private JLabel labelLabslist;

    private DefaultListModel modelLab = new DefaultListModel();
    private DefaultListModel modelBoard = new DefaultListModel();


    private void initLists(Client client) {
        List<Board> boards = client.requestBlockingServerBoardList();
        listLab.setSelectedIndex(0);
        listBoard.setModel(modelBoard);
        if (!boards.isEmpty()) {
            for (int i = 0; i < boards.size(); i++)
                modelBoard.addElement(boards.get(i));
        } else {
            modelBoard.addElement("No avaible boards");
        }
        listBoard.setSelectedIndex(0);
    }

    private void setSize(double height_inc, double weight_inc) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height * height_inc);
        int width = (int) (screenSize.width * weight_inc);
        this.setPreferredSize(new Dimension(width, height));
    }

    public AttachBoardForm(Client client, String ip, int port) {
        setSize(0.5, 0.5);
        this.setContentPane(this.mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setTitle("Require an avaible board");
        //Per ora rendo la lista dei laboratori non visibile
        this.listLab.setVisible(false);
        this.labelLabslist.setVisible(false);
        //

        listLab.setModel(modelLab);
        modelLab.addElement("Lab Claudio");
        initLists(client);


        buttonRequestSelectedBoard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listBoard.getSelectedValue().getClass().toString().contains("Board")) {
                    Board selectedBoard = (Board) listBoard.getSelectedValue();
                    client.requestBoard(selectedBoard.getSerialNumber());
                    new ChoiseForm(client, listLab.getSelectedValue().toString(), listBoard.getSelectedValue().toString(), ip, port);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Unable to make the request.", "No boards available.", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        refreshButtonBoard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modelBoard.clear();
                initLists(client);
            }
        });
    }

}