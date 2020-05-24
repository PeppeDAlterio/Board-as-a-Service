package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class RemoteFlashForm extends ClientJFrame {
    private JPanel mainPanel;
    private JTextField textFieldFlash;
    private JButton startFlashButton;
    JTextArea textAreaFlash;
    private JScrollPane scrollPaneTextArea;

    private void setSize(double height_inc, double weight_inc) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height * height_inc);
        int width = (int) (screenSize.width * weight_inc);
        this.setPreferredSize(new Dimension(width, height));
    }

    public RemoteFlashForm(Client client) {
        super("Remote flash - Client - Board as a Service");
        setSize(0.5, 0.5);
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        scrollPaneTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.setLocationRelativeTo(null);
        this.textAreaFlash.setEditable(false);
        this.textAreaFlash.setFont(new Font("courier", Font.BOLD, 12));

        startFlashButton.addActionListener(e -> {
                //TODO : Controlli su elf_file
                try {
                    client.requestFlash(textFieldFlash.getText());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
        });

    }

}
