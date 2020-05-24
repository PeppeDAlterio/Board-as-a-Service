package it.unina.sistemiembedded.main;

import it.unina.sistemiembedded.boundary.server.ServerListBoardGUIForm;
import it.unina.sistemiembedded.server.Server;
import it.unina.sistemiembedded.server.impl.ServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainServerGUIForm extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(MainServerGUIForm.class);

    private static Server server;

    private JPanel mainPanel;
    private JTextField textFieldname;
    private JTextField textFieldportnumber;
    private JButton startServerButton;

    private String nameServer = "Server-" + ((int) (Math.random() * 1000 + 1000));
    private int portNumber = 1234;

    private void setSize(double height_inc, double weight_inc) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height * height_inc);
        int width = (int) (screenSize.width * weight_inc);
        this.setPreferredSize(new Dimension(width, height));
    }

    public MainServerGUIForm() {
        super();

        if (server != null) {
            try {
                server.stop();
            } catch (IOException ignored) {
            }
        }

        setSize(0.2, 0.2);
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Server settings - Board as a Service");
        this.textFieldname.setText(nameServer);
        this.textFieldportnumber.setText(Integer.toString(portNumber));
        startServerButton.addActionListener(e -> {
            String name = textFieldname.getText();
            if (name.compareTo("") == 0) {
                name = nameServer;
            }
            if (textFieldportnumber.getText().compareTo("") == 0) {
                JOptionPane.showMessageDialog(this, "Port number must be an integer in the range of valid port values [ 0 , 65535 ]", "Invalid port number", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    portNumber = Integer.parseInt(textFieldportnumber.getText());
                    server = new ServerImpl(name, portNumber);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid port number: " + textFieldportnumber.getText(), "Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Server : " + name + " cannot start on port : " + portNumber, "Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                dispose();
                new ServerListBoardGUIForm(server);
            }
        });
        textFieldname.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                textFieldname.setText("");
            }
        });
        textFieldportnumber.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                textFieldportnumber.setText("");
            }
        });
    }

    public static void main(String[] args) {
        mainApplicationInit();
        new MainServerGUIForm();
    }

    private static void mainApplicationInit() {

        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "dd/MM/yyyy HH:mm:ss");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy - HH_mm_ss");
        Date date = new Date();
        File directory = new File("./log/");
        if (!directory.exists() && !directory.mkdir()) {
            throw new IllegalArgumentException("Non è possibile creare la directory" + directory.getPath());
        }
        if (directory.exists()) {
            System.out.println(directory.getPath() + " già esistente");
        }

        File file = new File(directory.getPath() + "/log[ " + formatter.format(date) + " ].txt");
        System.setProperty("org.slf4j.simpleLogger.logFile", file.getPath());

        final Thread shutdownThread = new Thread(() -> {
            logger.info("[shutdownhook] Shutdown hook started...");
            if (server != null) {
                try {
                    server.stop();
                } catch (IOException ignored) {
                }
            }
            logger.info("[shutdownhook] ...shutdown hook finished.");
        });

        Runtime.getRuntime().addShutdownHook(shutdownThread);

    }
}
