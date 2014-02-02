package server;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * This is a very simple GUI designed to help test
 * the server. It provides a text box for the
 * tester to type in commands that are sent to
 * the server. There is also an incoming
 * messages box that stores all messages 
 * received from the server.
 */
public class ServerTestGUI extends JFrame implements ActionListener, WindowListener{
    private JTextField input;
    private JTable output;
    private JScrollPane outputPane;
    
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;

    private final String serverName = "127.0.0.1";
    private final int portNumber = 4444;

    /**
     * Constructor for this ServerTestGUI.
     */
    public ServerTestGUI() {
        //Set up GUI, listeners, etc.
        setupGUI();

        //Create connection with server
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    client = new Socket(InetAddress.getByName(serverName), portNumber);
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    out = new PrintWriter(client.getOutputStream(), true);

                    String line = "";
                    while((line = in.readLine()) != null) {
                        ((DefaultTableModel) output.getModel()).addRow(new String[] {line});
                    }
                } catch (IOException e) {
                    client = null;
                    out = null;
                    System.out.println("IOException: " + e.getMessage());
                }
            }
        });

        thread.start();
    }

    /**
     * Lays out the UI and listeners for this GUI.
     */
    private void setupGUI() {
        input = new JTextField("Enter a server message.");
        input.addActionListener(this);

        output = new JTable(new DefaultTableModel(new String[] {"Received messages"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });

        outputPane = new JScrollPane(output);

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(input)
                        .addComponent(outputPane)));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(input)
                .addComponent(outputPane));

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.setMinimumSize(new Dimension(500,500));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Server Testing");
        this.addWindowListener(this);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        String text = input.getText();
        out.println(text);
        out.flush();
        input.setText("");
    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ServerTestGUI main = new ServerTestGUI();
                main.setVisible(true);
            }
        });
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {}

    @Override
    public void windowDeiconified(WindowEvent arg0) {}

    @Override
    public void windowIconified(WindowEvent arg0) {}

    @Override
    public void windowOpened(WindowEvent arg0) {}

    @Override
    public void windowActivated(WindowEvent arg0) {}

    @Override
    public void windowClosed(WindowEvent arg0) {}

}
