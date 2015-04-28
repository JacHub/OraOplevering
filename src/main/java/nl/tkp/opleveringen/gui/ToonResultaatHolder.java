package nl.tkp.opleveringen.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by eekhout.l on 28-04-2015.
 * class ToonResultaat
 */
public class ToonResultaatHolder implements AWTResizable {
    private JTextArea textArea1;
    private JPanel panel1;
    private JPanel controlPanel;
    private JButton buttonOK;

    public ToonResultaatHolder() {
    }

    public JPanel getPanel1() {
        return panel1;
    }

    @Override
    public void onResize(int width, int height) {
        textArea1.setLocation(0, 0);
        textArea1.setSize(width, 200);
    }

    @Override
    public void onLocation() {
        textArea1.setLocation(0, 0);
    }
}
