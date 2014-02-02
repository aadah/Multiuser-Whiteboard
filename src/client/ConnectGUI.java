package client;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * ConnectGUI is a JFrame that contains a simple interface for connecting to
 * a local or remote server. There are two options: connect to a remote server
 * using the text box by entering the IPv4 address of the machine running the
 * server, or connect to your own machine by hitting the `localhost' button
 * (assuming your are already running a server on your computer).
 * 
 * There are two constructors: one for testing the layout, and one for actual
 * use in the final application.
 */

/*
 * REPRESENTATION INVARIANT:
 * 
 *      No JComponent field is null.
 */
public class ConnectGUI extends JFrame {
    private final JLabel connectMsg;
    private final JLabel ipMsg;
    private final JTextField ipTxt;
    private final JLabel localMsg;
    private final JButton localBtn;
    
    /**
     * Creates a ConnectGUI.
     */
    public ConnectGUI() {
        connectMsg = new JLabel("Connect to an IPv4 address or localhost.");
        ipMsg = new JLabel("Specific IPv4 address:");
        ipTxt = new JTextField("127.0.0.1");
        localMsg = new JLabel("Or connect to localhost:");
        localBtn = new JButton("localhost");

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(connectMsg)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(ipMsg)
                        .addComponent(ipTxt))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(localMsg)
                        .addComponent(localBtn)));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(connectMsg)
                .addGroup(layout.createParallelGroup()
                        .addComponent(ipMsg)
                        .addComponent(ipTxt))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(localMsg)
                        .addComponent(localBtn)));
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Connect Beta");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    /**
     * Creates a ConnectGUI with the given JComponents.
     * 
     * @param connectMsg the connect message JLabel
     * @param ipMsg the IPv4 message JLabel
     * @param ipTxt the JTextField in which the IPv4 address is entered
     * @param localMsg the `localhost' message JLabel
     * @param localBtn the JButton that is pressed to connect to `localhost'
     */
    public ConnectGUI(JLabel connectMsg, JLabel ipMsg, JTextField ipTxt,
                        JLabel localMsg, JButton localBtn) {
        this.connectMsg = connectMsg;
        this.ipMsg = ipMsg;
        this.ipTxt = ipTxt;
        this.localMsg = localMsg;
        this.localBtn = localBtn;

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(this.connectMsg)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(this.ipMsg)
                        .addComponent(this.ipTxt))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(this.localMsg)
                        .addComponent(this.localBtn)));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(this.connectMsg)
                .addGroup(layout.createParallelGroup()
                        .addComponent(this.ipMsg)
                        .addComponent(this.ipTxt))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(this.localMsg)
                        .addComponent(this.localBtn)));
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Connect");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ConnectGUI main = new ConnectGUI();
                main.setVisible(true);
            }
        });
    }
}
