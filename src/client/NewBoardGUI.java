package client;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * NewBoardGUI provides the interface in which the client enters the parameters
 * needed to create a new board. The parameters are name, width, and height. In
 * the main application, the client is constrained to enter dimensions that are 
 * at least 250 pixels in both directions, and enter a board name that is not
 * already taken.
 * 
 * There are two constructors: one for testing the layout, and one for actual
 * use in the final application.ion.
 */

/*
 * REPRESENTATION INVARIANT:
 * 
 *      No JComponent field is null.
 */
public class NewBoardGUI extends JFrame {
    private final JLabel boardMsg;
    private final JLabel nameMsg;
    private final JTextField nameTxt;
    private final JLabel widthMsg;
    private final JSpinner widthSpnr;
    private final JLabel heightMsg;
    private final JSpinner heightSpnr;
    private final JButton okBtn;
    
    /**
     * Creates a NewBoardGUI.
     */
    public NewBoardGUI() {
        boardMsg = new JLabel("Create a new whiteboard. Enter a name and dimensions.");
        nameMsg = new JLabel("Name:");
        nameTxt = new JTextField("Untitled");
        widthMsg = new JLabel("Width:");
        widthSpnr = new JSpinner();
        widthSpnr.setValue(640);
        heightMsg = new JLabel("Height:");
        heightSpnr = new JSpinner();
        heightSpnr.setValue(480);
        okBtn = new JButton("OK");

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(boardMsg,GroupLayout.Alignment.CENTER)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(nameMsg)
                        .addComponent(nameTxt))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(widthMsg)
                        .addComponent(widthSpnr))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(heightMsg)
                        .addComponent(heightSpnr))
                .addComponent(okBtn,GroupLayout.Alignment.CENTER));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(boardMsg)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(nameMsg)
                        .addComponent(nameTxt))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(widthMsg)
                        .addComponent(widthSpnr))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(heightMsg)
                        .addComponent(heightSpnr))
                .addComponent(okBtn));
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("New Whiteboard Beta");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    /**
     * Creates a NewBoardGUI with the given JComponents.
     * 
     * @param boardMsg the JLabel message prompting the client what to do
     * @param nameMsg the JLabel message telling the client where to enter the name
     * @param nameTxt the JTextField where the client inputs the name
     * @param widthMsg the JLabel message telling the client where to enter the width
     * @param widthSpnr the JSpinner that lets the client set the width
     * @param heightMsg the JLabel message telling the client where to enter the height
     * @param heightSpnr the JSpinner that lets the client set the height
     * @param okBtn the JButton the client presses to confirm there settings
     */
    public NewBoardGUI(JLabel boardMsg, JLabel nameMsg, JTextField nameTxt, JLabel widthMsg,
                        JSpinner widthSpnr, JLabel heightMsg, JSpinner heightSpnr, JButton okBtn) {
        this.boardMsg = boardMsg;
        this.nameMsg = nameMsg;
        this.nameTxt = nameTxt;
        this.widthMsg = widthMsg;
        this.widthSpnr = widthSpnr;
        this.widthSpnr.setValue(640);
        this.heightMsg = heightMsg;
        this.heightSpnr = heightSpnr;
        this.heightSpnr.setValue(480);
        this.okBtn = okBtn;

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(this.boardMsg, GroupLayout.Alignment.CENTER)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(this.nameMsg)
                        .addComponent(this.nameTxt))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(this.widthMsg)
                        .addComponent(this.widthSpnr))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(this.heightMsg)
                        .addComponent(this.heightSpnr))
                .addComponent(okBtn,GroupLayout.Alignment.CENTER));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(this.boardMsg)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(this.nameMsg)
                        .addComponent(this.nameTxt))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(this.widthMsg)
                        .addComponent(this.widthSpnr))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(this.heightMsg)
                        .addComponent(this.heightSpnr))
                .addComponent(okBtn));
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("New");
    }
    
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NewBoardGUI main = new NewBoardGUI();
                main.setVisible(true);
            }
        });
    }
}
