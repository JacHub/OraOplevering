package nl.tkp.opleveringen.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.Transient;

/**
 * Created by eekhout.l on 28-04-2015.
 * class ToonResultaatJFrame
 */
public class ToonResultaatJFrame extends JFrame {

    private AWTResizable resizable;

    public ToonResultaatJFrame(String title, AWTResizable resizable) {
        super(title);
        this.resizable = resizable;
        System.out.println("ToonResultaatJFrame(\"" + title + "\")");
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                ToonResultaatJFrame.this.resizable.onResize(e.getComponent().getWidth(), e.getComponent().getHeight());
            }
        });
    }

    @Override
    public void setContentPane(Container contentPane) {
        resizable.onLocation();
        super.setContentPane(contentPane);
    }
}
