package client;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * NameGUI contains an input option for a client's username. When used in the main
 * application, it will prompt for another name if the  previously given one is
 * invalid of already taken by another client.
 * 
 * There are two constructors: one for testing the layout, and one for actual
 * use in the final application.
 */

/*
 * REPRESENTATION INVARIANT:
 * 
 *      No JComponent field is null.
 */
public class NameGUI extends JFrame {
    private final JLabel startMsg;
    private final JTextField startTxt;
    
    /**
     * Creates a NameGUI.
     */
    public NameGUI() {
        startMsg = new JLabel("Please enter a username (no spaces). Hit enter when done.");
        startTxt = new JTextField();
        
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(startMsg)
                .addComponent(startTxt));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(startMsg)
                .addComponent(startTxt));
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Startup Beta");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    /**
     * Creates a NameGUI with the given JComponents.
     * 
     * @param startMsg the JLabel prompt message telling the client what to do
     * @param startTxt the JTextField where the client enters their username
     */
    public NameGUI(JLabel startMsg, JTextField startTxt) {
        this.startMsg = startMsg;
        this.startTxt = startTxt;
        
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(this.startMsg)
                .addComponent(this.startTxt));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(this.startMsg)
                .addComponent(this.startTxt));
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Set Username");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NameGUI main = new NameGUI();
                main.setVisible(true);
            }
        });
    }
}
