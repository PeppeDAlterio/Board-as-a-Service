package it.unina.sistemiembedded.boundary.client;

import it.unina.sistemiembedded.client.Client;
import it.unina.sistemiembedded.utility.ui.UILongRunningHelper;
import it.unina.sistemiembedded.utility.ui.UISizeHelper;
import it.unina.sistemiembedded.utility.ui.stream.UIPrinterHelper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class RemoteFlashForm extends ClientJFrame {
    private JPanel mainPanel;
    private JTextField textFieldFlash;
    private JButton startFlashButton;
    JTextArea textAreaFlash;
    private JScrollPane scrollPaneTextArea;
    private JButton browseButton;

    private final JFileChooser jFileChooser;

    public RemoteFlashForm(Client client) {
        super("Remote flash - Client - Board as a Service");
        UISizeHelper.setSize(this,0.5, 0.5);
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        scrollPaneTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.setLocationRelativeTo(null);
        this.textAreaFlash.setEditable(false);
        this.textAreaFlash.setFont(new Font("courier", Font.BOLD, 12));
        this.textFieldFlash.setEditable(false);

        jFileChooser = new JFileChooser();
        jFileChooser.setCurrentDirectory(new File("C://"));
        jFileChooser.setFileFilter(new FileNameExtensionFilter(".elf","elf"));
        jFileChooser.setDialogTitle("Chose a file.");

        startFlashButton.addActionListener(e -> {
                //TODO : Controlli su elf_file
                String elf_file = textFieldFlash.getText();
                UILongRunningHelper.<Exception>supplyAsync(this, "Flashing file : " + elf_file, () -> {
                    try {
                        client.requestBlockingFlash(elf_file);
                    }catch (IllegalArgumentException ex){
                        return ex;
                    } catch (IOException ex) {
                        return ex;
                    }
                    return null;
                }, result ->{
                    if(result instanceof IllegalArgumentException){
                        JOptionPane.showMessageDialog(this,"File "+elf_file+" does't exists","Error!",JOptionPane.ERROR_MESSAGE);
                    }else if(result instanceof IOException){
                        JOptionPane.showMessageDialog(this,"There was an error during flashing operation.","Error!",JOptionPane.ERROR_MESSAGE);
                    }else{
                        UIPrinterHelper.clientFlash("Flash of file '"+elf_file+"' completed");
                        //TODO : Aggiungere più info sul flash
                    }
                });
        });


        textFieldFlash.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                textFieldFlash.setText("");
            }
        });

        browseButton.addActionListener(e -> {
                int value = jFileChooser.showOpenDialog(this);
                if(value == JFileChooser.APPROVE_OPTION){
                    String file_selected = jFileChooser.getSelectedFile().getAbsolutePath();
                    textFieldFlash.setText(file_selected);
                }else{
                    textFieldFlash.setText("No file selected.");
                }
        });
    }

}
