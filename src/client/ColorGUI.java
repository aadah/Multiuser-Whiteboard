package client;
import java.awt.Color;

import javax.swing.GroupLayout;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * ColorGUI is a JFrame that contains only a JColorChooser. It's purpose is
 * to provide a simple window for clients to choose their color when drawing
 * on the canvas.
 * 
 * There are two constructors: one for testing the layout, and one for actual
 * use in the final application.
 */

/*
 * REPRESENTATION INVARIANT:
 * 
 *      No JComponent field is null.
 */
public class ColorGUI extends JFrame {
    private final JColorChooser palette;
    
    /**
     * Creates a ColorGUI.
     */
    public ColorGUI() {
        palette = new JColorChooser(new Color(0,0,0));
        
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(palette));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(palette));

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Color Beta");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    /**
     * Creates a JFrame with the given JColorChooser.
     * 
     * @param palette
     */
    public ColorGUI(JColorChooser palette) {
        this.palette = palette;
        
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(this.palette));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(this.palette));

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.pack();
        this.setTitle("Palette");
    }
    
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ColorGUI main = new ColorGUI();
                main.setVisible(true);
            }
        });
    }
}
