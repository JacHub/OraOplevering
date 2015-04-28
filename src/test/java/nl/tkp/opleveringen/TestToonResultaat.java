package nl.tkp.opleveringen;

import javax.swing.*;
import java.awt.*;
import nl.tkp.opleveringen.gui.ToonResultaatHolder;
import nl.tkp.opleveringen.gui.ToonResultaatJFrame;

/**
 * Created by eekhout.l on 28-04-2015.
 * class TestToonResultaat
 */
public class TestToonResultaat {
    public static void main(String[] args) {
        ToonResultaatHolder holder = new ToonResultaatHolder();
        ToonResultaatJFrame frame = new ToonResultaatJFrame("OraOplevertool", holder);
        frame.setPreferredSize(new Dimension(640, 480));
        frame.setContentPane(holder.getPanel1());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
