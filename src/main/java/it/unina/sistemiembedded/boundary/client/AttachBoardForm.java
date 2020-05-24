package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.exception.BoardAlreadyInUseException;
import it.unina.sistemiembedded.exception.BoardNotFoundException;
import it.unina.sistemiembedded.main.MainClientGUIForm;
import it.unina.sistemiembedded.model.Board;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

@Getter
@Setter
public class AttachBoardForm extends ClientJFrame {
    private JPanel mainPanel;
    private JButton buttonRequestSelectedBoard;
    private JList listBoard;
    private JList listLab;
    private JButton refreshButtonBoard;
    private JLabel labelLabslist;

    private DefaultListModel modelLab = new DefaultListModel();
    private DefaultListModel modelBoard = new DefaultListModel();

    private final ClientJFrame $this=this;


    private void initLists(Client client) {
        if (client.isConnected()) {
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
    }

    private void setSize(double height_inc, double weight_inc) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height * height_inc);
        int width = (int) (screenSize.width * weight_inc);
        this.setPreferredSize(new Dimension(width, height));
    }

    public AttachBoardForm(Client client, String ip, int port) {
        super("Server board list - Client - Board as a Service");
        System.out.println(ClientJFrame.getActiveFrames());
        setSize(0.5, 0.5);
        this.setContentPane(this.mainPanel);
        this.setVisible(true);
        this.pack();
        this.setLocationRelativeTo(null);
        //Per ora rendo la lista dei laboratori non visibile
        this.listLab.setVisible(true);
        this.labelLabslist.setVisible(true);
        //

        listLab.setModel(modelLab);
        modelLab.addElement(client.getServerName()+" [ "+ip+":"+port+" ]");
        initLists(client);


        buttonRequestSelectedBoard.addActionListener(e -> {
            if (listBoard.getSelectedValue().getClass().toString().contains("Board")) {
                Board selectedBoard = (Board) listBoard.getSelectedValue();
                try {
                    client.requestBlockingBoard(selectedBoard.getSerialNumber());
                    new ChoiseForm(client, listBoard.getSelectedValue().toString(), ip, port, this);
                    this.dispose();
                } catch (BoardNotFoundException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "The selected board doesn't exists", "Board not found", JOptionPane.ERROR_MESSAGE);
                } catch (BoardAlreadyInUseException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "The selected board is already in use by another client", "Board already in use", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Unable to make the request.", "No boards available.", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshButtonBoard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modelBoard.clear();
                initLists(client);
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                    JOptionPane.showMessageDialog($this, "Your connection whit "+client.getServerName()+
                            " [ "+ip+":"+port+" ] will be closed", "Closing connection...", JOptionPane.WARNING_MESSAGE);
                    client.disconnect();
                    dispose();
                    new MainClientGUIForm();
            }
        });
    }

}