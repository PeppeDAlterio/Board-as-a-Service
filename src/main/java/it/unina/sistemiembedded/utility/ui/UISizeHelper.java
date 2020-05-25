package it.unina.sistemiembedded.utility.ui;

import javax.swing.*;
import java.awt.*;


public class UISizeHelper {
    /**
     * Set a new size for a frame increasing the current size in functions of height_inc and weight_inc
     * @params  frame : frame that needs to be resized
     *          height_inc : height increase
     *          weight_inc : weight increase
     */
    public static void setSize(JFrame frame , double height_inc, double weight_inc) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height * height_inc);
        int width = (int) (screenSize.width * weight_inc);
        frame.setPreferredSize(new Dimension(width, height));
    }
}
