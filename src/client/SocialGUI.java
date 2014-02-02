package client;

import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * SocialGUI is one of the more important windows. It contains tables that show
 * available boards, users on the client's same board, and a chat window for
 * communicating with said users. In the main application, these tables are
 * updated dynamically as new information is received from the server. The
 * window also has options for changing your username and creating a new board.
 * 
 * There are two constructors: one for testing the layout, and one for actual
 * use in the final application.
 */

/*
 * REPRESENTATION INVARIANT:
 * 
 *      No JComponent field is null.
 */
public class SocialGUI extends JFrame {
    private final JTable users;
    private final JTable boards;
    private final JTable chat;
    private final JScrollPane userPane;
    private final JScrollPane boardPane;
    private final JScrollPane chatPane;
    private final JTextField chatTxt;
    private final JButton newBoardBtn;
    private final JButton changeUserBtn;
    
    /**
     * Creates a SocialGUI.
     */
    public SocialGUI() {
        users = new JTable(new DefaultTableModel(new String[] {"Users"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        boards = new JTable(new DefaultTableModel(new String[] {"Boards"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        chat = new JTable(new DefaultTableModel(new String[] {"User","Time","Message"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        userPane = new JScrollPane(users);
        boardPane = new JScrollPane(boards);
        chatPane  = new JScrollPane(chat);
        chatTxt = new JTextField();
        newBoardBtn = new JButton("New Board");
        changeUserBtn = new JButton("Change Username");

        TableColumn column;
        for (int i = 0; i < 3; i++) {
            column = chat.getColumnModel().getColumn(i);
            if (i == 2) {
                column.setPreferredWidth(400);
            }
            else {
                column.setPreferredWidth(75);
            }
        }
        
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(userPane)
                        .addComponent(boardPane))
                .addComponent(chatPane)
                .addComponent(chatTxt)
                .addComponent(newBoardBtn)
                .addComponent(changeUserBtn));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(userPane)
                        .addComponent(boardPane))
                .addComponent(chatPane)
                .addComponent(chatTxt)
                .addComponent(newBoardBtn)
                .addComponent(changeUserBtn));
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.setMinimumSize(new Dimension(500,500));
        this.setSize(new Dimension(500,750));
        this.setLocationRelativeTo(null);
        this.setTitle("Social Beta");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    /**
     * Creates a SocialGUI with the given JComponenets.
     * 
     * @param users the JTable containing names of fellow users editing the same board
     * @param boards the JTable containing all available boards
     * @param chat the JTable containing chat messages for a certain board
     * @param userpane the JScrollPane containing users
     * @param boardpane the JScrollPane containing boards
     * @param chatPane the JScrollPane containing chat
     * @param chatTxt the JTextField where the client enters their chat message
     * @param newBoardBtn the JButton the client presses if they wish to create a new board
     * @param changeUserBtn the JBUtton the client presses if the wish to change their username
     */
    public SocialGUI(JTable users, JTable boards, JTable chat, JScrollPane userpane, JScrollPane boardpane,
                        JScrollPane chatPane, JTextField chatTxt, JButton newBoardBtn, JButton changeUserBtn) {
        this.users = users;
        this.boards = boards;
        this.chat = chat;
        this.userPane = userpane;
        this.boardPane = boardpane;
        this.chatPane = chatPane;
        this.chatTxt = chatTxt;
        this.newBoardBtn = newBoardBtn;
        this.changeUserBtn = changeUserBtn;
        
        TableColumn column;
        for (int i = 0; i < 3; i++) {
            column = chat.getColumnModel().getColumn(i);
            if (i == 2) {
                column.setPreferredWidth(400);
            }
            else {
                column.setPreferredWidth(75);
            }
        }

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(this.userPane)
                        .addComponent(this.boardPane))
                .addComponent(this.chatPane)
                .addComponent(this.chatTxt)
                .addComponent(this.newBoardBtn)
                .addComponent(this.changeUserBtn));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(this.userPane)
                        .addComponent(this.boardPane))
                .addComponent(this.chatPane)
                .addComponent(this.chatTxt)
                .addComponent(this.newBoardBtn)
                .addComponent(this.changeUserBtn));
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.setMinimumSize(new Dimension(500,500));
        this.setSize(new Dimension(500,750));
        this.setTitle("Users/Boards");
    }
    
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SocialGUI main = new SocialGUI();
                main.setVisible(true);
            }
        });
    }
}
