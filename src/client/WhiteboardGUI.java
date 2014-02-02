package client;

import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import canvas.Canvas;

/**
 * WhiteboardGUI is the main visual element of the program. It contains the
 * actual canvas in which the client draws to. It also contains the buttons
 * the client presses to bring up the other necessary window like the editor
 * and social windows. Closing this window will disconnect the user and
 * terminate their session.
 * 
 * There are two constructors: one for testing the layout, and one for actual
 * use in the final application.
 */

/*
 * REPRESENTATION INVARIANT:
 * 
 *      No JComponent field is null.
 */
public class WhiteboardGUI extends JFrame {
    private final Canvas canvas;
    private final JScrollPane canvasPane;
    private final JButton editorBtn;
    private final JButton colorBtn;
    private final JButton socialBtn;
    private final JButton saveBtn;
    
    /**
     * Creates a WhiteboardGUI.
     */
    public WhiteboardGUI() {
        canvas = new Canvas();
        canvasPane = new JScrollPane(canvas);
        editorBtn = new JButton("Editor");
        colorBtn = new JButton("Palette");
        socialBtn = new JButton("Users/Boards");
        saveBtn = new JButton("Save");
        
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(canvasPane)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(editorBtn)
                        .addComponent(colorBtn)
                        .addComponent(socialBtn)
                        .addComponent(saveBtn)));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(canvasPane)
                .addGroup(layout.createParallelGroup()
                        .addComponent(editorBtn)
                        .addComponent(colorBtn)
                        .addComponent(socialBtn)
                        .addComponent(saveBtn)));
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.setMinimumSize(new Dimension(500,500));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Whiteboard3 Beta");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    /**
     * Creates a WhiteboardGUI with the given JComponents.
     * 
     * @param canvas the Canvas instance that the client will draw one
     * @param canvasPane the JScrollPane that contains the canvas
     * @param editorBtn the JButton the client presses to bring up the editor window
     * @param colorBtn the JButton the client presses to bring up the color palette
     * @param socialBtn the JButton the client presses to bring up the social window
     * @param saveBtn the JButton the client presses to bring up the save dialog box
     */
    public WhiteboardGUI(Canvas canvas, JScrollPane canvasPane, JButton editorBtn,
                            JButton colorBtn, JButton socialBtn, JButton saveBtn) {
        this.canvas = canvas;
        this.canvasPane = canvasPane;
        this.editorBtn = editorBtn;
        this.colorBtn = colorBtn;
        this.socialBtn = socialBtn;
        this.saveBtn = saveBtn;
        
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(this.canvasPane)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(this.editorBtn)
                        .addComponent(this.colorBtn)
                        .addComponent(this.socialBtn)
                        .addComponent(this.saveBtn)));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(this.canvasPane)
                .addGroup(layout.createParallelGroup()
                        .addComponent(this.editorBtn)
                        .addComponent(this.colorBtn)
                        .addComponent(this.socialBtn)
                        .addComponent(this.saveBtn)));
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Whiteboard");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                WhiteboardGUI main = new WhiteboardGUI();
                main.setVisible(true);
            }
        });
    }
}