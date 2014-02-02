package client;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * SaveGUI is simply a prompt GUI that is activated when the user is trying
 * to save a file with the same name and directory as another. It's very
 * straightforward, `yes' to overwrite the existing file, `no' to not overwrite.
 * 
 * There are two constructors: one for testing the layout, and one for
 * actual use in the final application.
 */

/*
 * REPRESENTATION INVARIANT:
 * 
 *      No JComponent field is null.
 */
public class SaveGUI extends JFrame {
    private final JLabel saveMsg;
    private final JButton yesBtn;
    private final JButton noBtn;
    
    /**
     * Creates a SaveGUI.
     */
    public SaveGUI() {
        saveMsg = new JLabel("A file with that name already exists. Overwrite?");
        yesBtn = new JButton("Yes");
        noBtn = new JButton("No");
        
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(saveMsg)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(yesBtn)
                        .addComponent(noBtn)));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(saveMsg)
                .addGroup(layout.createParallelGroup()
                        .addComponent(yesBtn)
                        .addComponent(noBtn)));

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Save Beta");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    /**
     * Creates a SaveGUI with the given JComponents.
     * 
     * @param saveMsg the JLabel message asking the client if they wish to overwrite
     * @param yesBtn the JButton the client presses if they wish to overwrite
     * @param noBtn the JButton the client presses if they don't wish to overwrite
     */
    public SaveGUI(JLabel saveMsg, JButton yesBtn, JButton noBtn) {
        this.saveMsg = saveMsg;;
        this.yesBtn = yesBtn;
        this.noBtn = noBtn;
        
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(this.saveMsg)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(this.yesBtn)
                        .addComponent(this.noBtn)));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(this.saveMsg)
                .addGroup(layout.createParallelGroup()
                        .addComponent(this.yesBtn)
                        .addComponent(this.noBtn)));

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Are you sure?");
    }
    
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SaveGUI main = new SaveGUI();
                main.setVisible(true);
            }
        });
    }
}
