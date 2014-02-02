package client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import canvas.Canvas;

/**
 * MasterGUI is itself not actually a GUI, but a variety of listeners. However,
 * it employs composition to the degree that it effectively acts as one, in
 * combination with its implemented listener methods. The main application is
 * started by running this file.
 * 
 * The basic use:
 *      When run, the user sees a window asking how they wish to connect. Once
 * that has been completed, the next window asks for a username (which can be
 * changed any time during the use of the application). Finally, the user is
 * brought to the actual window containing the editable whiteboard, with a clean
 * UI for opening an editor window for choosing modes, a color palette, and even
 * a social chat window to communicate with fellow editors. The user can also easily
 * save their masterpiece whenever they want with a simple press of save button.
 */

/*
 * THREAD-SAFETY ARGUMENT
 * 
 * MasterGUI is made thread-safe by simply wrapping any message-sending method 
 * used on canvas in a SwingWorker thread. Computationally expensive methods
 * (e.g. canvas.saveImage()) were also wrapped in SwingWorker threads.
 */

/*
 * REPRESENTATION INVARIANT:
 * 
 *      No JComponent field is null.
 */
public class MasterGUI implements ActionListener, ChangeListener, ListSelectionListener, WindowListener {
    // GUI windows
    private final ConnectGUI CONNECT; // contains options for where to connect to
    private final NameGUI NAME; // contains entry point for personal username
    private final WhiteboardGUI WHITEBOARD; // contains the whiteboard and buttons for other windows
    private final EditorGUI EDITOR; // contains drawing mode options and stroke size settings
    private final ColorGUI COLOR; // contains color palette for user-chosen colors
    private final SocialGUI SOCIAL; // contains chat, new username/board options, and tables of available boards and online users
    private final NewBoardGUI NEW; // contains parameters (name,width,height) for creating a new whiteboard
    private final SaveGUI SAVE; // a small prompt that comes up asking if a user wishes to overwrite when saving
    
    //ConnectGUI JComponents
    private final JLabel connectMsg;
    private final JLabel ipMsg;
    private final JTextField ipTxt;
    private final JLabel localMsg;
    private final JButton localBtn;
    
    // NameGUI JComponents
    private final JLabel startMsg;
    private final JTextField startTxt;
    
    //  WhiteboardGUI JCompenents
    private final Canvas canvas;
    private final JScrollPane canvasPane;
    private final JButton editorBtn;
    private final JButton colorBtn;
    private final JButton socialBtn;
    private final JButton saveBtn;
    
    // EditorGUI JCompenents
    private final JLabel modeMsg;
    private final JRadioButton pencilRdioBtn;
    private final JRadioButton segmentRdioBtn;
    private final JRadioButton fillRdioBtn;
    private final JRadioButton eraseRdioBtn;
    private final ButtonGroup buttonGrp;
    private final JLabel strokeSizeMsg;
    private final JSlider strokeSizeSldr;
    
    // ColorGUI JComponents
    private final JColorChooser palette;
    
    // SocialGUI JComponents
    private final JTable users;
    private final JTable boards;
    private final JTable chat;
    private final JScrollPane userPane;
    private final JScrollPane boardPane;
    private final JScrollPane chatPane;
    private final JTextField chatTxt;
    private final JButton newBoardBtn;
    private final JButton changeUserBtn;
    
    // NewBoardGUI JComponents
    private final JLabel boardMsg;
    private final JLabel nameMsg;
    private final JTextField nameTxt;
    private final JLabel widthMsg;
    private final JSpinner widthSpnr;
    private final JLabel heightMsg;
    private final JSpinner heightSpnr;
    private final JButton okBtn;
    
    // SaveGUI JComponents
    private final JLabel saveMsg;
    private final JButton yesBtn;
    private final JButton noBtn;
    private final JFileChooser chooser; // not actually put into the GUI, we just use showSaveDialog()
    
    /**
     * Creates the MasterGUI. Initializes all JComponents and adds listeners where needed.
     */
    public MasterGUI() {
        // Create ConnectGUI
        connectMsg = new JLabel("Connect to an IPv4 address or localhost.");
        ipMsg = new JLabel("Specific IPv4 address:");
        ipTxt = new JTextField("127.0.0.1");
        ipTxt.addActionListener(this);
        localMsg = new JLabel("Or connect to localhost:");
        localBtn = new JButton("localhost");
        localBtn.addActionListener(this);
        CONNECT = new ConnectGUI(connectMsg,ipMsg,ipTxt,localMsg,localBtn);
        
        // Create NameGUI
        startMsg = new JLabel("Please enter a username (no spaces). Hit enter when done.");
        startTxt = new JTextField();
        startTxt.addActionListener(this);
        NAME = new NameGUI(startMsg,startTxt);
        NAME.addWindowListener(this);
        
        // Create WhiteboardGUI
        canvas = new Canvas();
        canvasPane = new JScrollPane(canvas);
        editorBtn = new JButton("Editor");
        editorBtn.addActionListener(this);
        colorBtn = new JButton("Palette");
        colorBtn.addActionListener(this);
        socialBtn = new JButton("Users/Boards");
        socialBtn.addActionListener(this);
        saveBtn = new JButton("Save");
        saveBtn.addActionListener(this);
        WHITEBOARD = new WhiteboardGUI(canvas,canvasPane,editorBtn,colorBtn,socialBtn,saveBtn);
        WHITEBOARD.setTitle("Whiteboard - IHTFP");
        WHITEBOARD.setMinimumSize(new Dimension(500,500));
        WHITEBOARD.setSize(new Dimension(700,700));
        WHITEBOARD.setLocationRelativeTo(null);

        // Create EditorGUI
        modeMsg = new JLabel("Mode:");
        pencilRdioBtn = new JRadioButton("Pencil");
        pencilRdioBtn.addActionListener(this);
        pencilRdioBtn.setActionCommand("pencil");
        segmentRdioBtn = new JRadioButton("Line");
        segmentRdioBtn.addActionListener(this);
        segmentRdioBtn.setActionCommand("segment");
        fillRdioBtn = new JRadioButton("Fill");
        fillRdioBtn.addActionListener(this);
        fillRdioBtn.setActionCommand("fill");
        eraseRdioBtn = new JRadioButton("Erase");
        eraseRdioBtn.addActionListener(this);
        eraseRdioBtn.setActionCommand("erase");
        buttonGrp = new ButtonGroup();
        strokeSizeMsg = new JLabel("Stroke size:");
        strokeSizeSldr = new JSlider(0,100,1);
        strokeSizeSldr.addChangeListener(this);
        EDITOR = new EditorGUI(modeMsg,pencilRdioBtn,segmentRdioBtn,fillRdioBtn,
                                eraseRdioBtn,buttonGrp,strokeSizeMsg,strokeSizeSldr);
        EDITOR.setLocationRelativeTo(null);

        // Create ColorGUI
        palette = new JColorChooser(new Color(0,0,0));
        palette.getSelectionModel().addChangeListener(this);
        COLOR = new ColorGUI(palette);
        COLOR.setLocationRelativeTo(null);

        // Create SocialGUI
        users = new JTable(canvas.getUsersTableModel());
        boards = new JTable(canvas.getBoardsTableModel());
        boards.getSelectionModel().addListSelectionListener(this);
        boards.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chat = new JTable(canvas.getChatTableModel());
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
        userPane = new JScrollPane(users);
        boardPane = new JScrollPane(boards);
        chatPane = new JScrollPane(chat);
        chatTxt = new JTextField();
        chatTxt.addActionListener(this);
        newBoardBtn = new JButton("New Board");
        newBoardBtn.addActionListener(this);
        changeUserBtn = new JButton("Change Username");
        changeUserBtn.addActionListener(this);
        SOCIAL = new SocialGUI(users,boards,chat,userPane,boardPane,chatPane, chatTxt, newBoardBtn,changeUserBtn);
        SOCIAL.setLocationRelativeTo(null);

        // Create NewBoardGUI
        boardMsg = new JLabel("Create a new whiteboard. Enter a name and dimensions.");
        nameMsg = new JLabel("Name:");
        nameTxt = new JTextField("Untitled");
        nameTxt.addActionListener(this);
        widthMsg = new JLabel("Width:");
        widthSpnr = new JSpinner();
        heightMsg = new JLabel("Height:");
        heightSpnr = new JSpinner();
        okBtn = new JButton("OK");
        okBtn.addActionListener(this);
        NEW = new NewBoardGUI(boardMsg,nameMsg,nameTxt,widthMsg,widthSpnr,heightMsg,heightSpnr,okBtn);
        NEW.addWindowListener(this);

        // Create SaveGUI
        saveMsg = new JLabel("A file with that name already exists. Overwrite?");
        yesBtn = new JButton("Yes");
        yesBtn.addActionListener(this);
        noBtn = new JButton("No");
        noBtn.addActionListener(this);
        SAVE = new SaveGUI(saveMsg,yesBtn,noBtn);
        SAVE.setLocationRelativeTo(null);
        chooser = new JFileChooser();
        chooser.setDialogTitle("Choose where to save.");

        // Initialize some final settings
        canvas.setCanvasColor(palette.getColor());
        canvas.setCanvasStrokeWidth(strokeSizeSldr.getValue());
    }
    
    /**
     * Starts the MasterGUI application.
     */
    public void startup() {
        CONNECT.setVisible(true);
    }
    
    /**
     * Enables/disables the whiteboard, editor, color, and social windows.
     * 
     * @param enable boolean representing whether windows are enabled
     */
    public void enable(boolean enable) {
        WHITEBOARD.setEnabled(enable);
        EDITOR.setEnabled(enable);
        COLOR.setEnabled(enable);
        SOCIAL.setEnabled(enable);
    }
    
    /**
     * This listener function deals with the larger portion of the interaction
     * between GUI windows, such as opening/closing certain windows and forwarding
     * user input to the backend model.
     * 
     * @param e the event that triggered the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == ipTxt || source == localBtn) {
            // in this conditional branch, we extract the inputed IPv4 address
            String ipAddress;
            if (source == ipTxt) {
                ipAddress = ipTxt.getText();
            }
            else {
                ipAddress = "localhost";
            }
            canvas.connectToServer(ipAddress); // here, we actually attempt to connect
            CONNECT.setVisible(false);
            NAME.setVisible(true);
        }
        else if (source == startTxt) {
            // in here, the user can set (or reset) their username
            SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
                @Override
                public Object doInBackground() {
                    String username = startTxt.getText();
                    if (username.contains(" ") || username.equals("")) {
                        startMsg.setText("Username cannot have spaces or be empty.");
                        NAME.pack();
                        NAME.setLocationRelativeTo(null);
                        return -1;
                    }
                    if (!(canvas.userChangeName(username))) {
                        startMsg.setText("Username already taken");
                        NAME.pack();
                        NAME.setLocationRelativeTo(null);
                        return -1;
                    }
                    SOCIAL.setTitle(username);
                    if (!WHITEBOARD.isVisible()) {
                        NAME.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        WHITEBOARD.setVisible(true);
                    }
                    return 1;
                }
                
                @Override
                public void done() {
                    int result;
                    try {
                        result = (int) get();
                    } catch (InterruptedException | ExecutionException e) {
                        result =  -1;
                    }
                    if (result == 1) {
                        enable(true);
                        NAME.setVisible(false);
                        startMsg.setText("Please enter a username (no spaces). Hit enter when done.");
                        NAME.pack();
                        NAME.setLocationRelativeTo(null);
                    }
                }
            };
            
            worker.execute();
        }
        else if (source == editorBtn) {
            EDITOR.setVisible(true);
        }
        else if (source == colorBtn) {
            COLOR.setVisible(true);
        }
        else if (source == socialBtn) {
            SOCIAL.setVisible(true);
        }
        else if (source == saveBtn) {
            SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
                @Override
                public Object doInBackground() {
                    int selection = chooser.showSaveDialog(saveBtn);
                    
                    if (selection == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
                        boolean notTaken = canvas.saveImage(file,false);
                        if (!notTaken) {
                            SAVE.setVisible(true);
                            enable(false);
                        }
                    }
                    return 1;
                }
            };
            
            worker.execute();
        }
        else if (source == changeUserBtn) {
            NAME.setVisible(true);
            this.enable(false);
        }
        else if (source == newBoardBtn) {
            NEW.setVisible(true);
            this.enable(false);
        }
        else if (source == pencilRdioBtn || source == segmentRdioBtn ||
                    source == fillRdioBtn || source == eraseRdioBtn) {
            canvas.setCanvasMode(((JRadioButton) source).getActionCommand());
        }
        else if (source == okBtn || source == nameTxt) {
            // in here, the user can create a new whiteboard
            SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
                @Override
                public Object doInBackground() {
                    String boardname = nameTxt.getText();
                    int width = (int) widthSpnr.getValue();
                    int height = (int) heightSpnr.getValue();
                    if (width < 250 || height < 250) {
                        boardMsg.setText("Dimensions must be at least 250 pixels each.");
                        NEW.pack();
                        NEW.setLocationRelativeTo(null);
                        return -1;
                    }
                    if (width > 4000 || height > 4000) {
                        boardMsg.setText("Dimensions must be at most 4000 pixels each.");
                        NEW.pack();
                        NEW.setLocationRelativeTo(null);
                        return -1;
                    }
                    if (boardname.contains(" ") || boardname.equals("")) {
                        boardMsg.setText("Board name cannot have spaces or be empty.");
                        NEW.pack();
                        NEW.setLocationRelativeTo(null);
                        return -1;
                    }
                    if (!(canvas.addBoard(boardname, width, height))) {
                        boardMsg.setText("Board name already taken.");
                        NEW.pack();
                        NEW.setLocationRelativeTo(null);
                        return -1;
                    }
                    canvas.setPreferredSize(new Dimension(width, height));
                    WHITEBOARD.setTitle("Whiteboard - " + boardname);
                    WHITEBOARD.pack();
                    return 1;
                }
                
                @Override
                public void done() {
                    int result;
                    try {
                        result = (int) get();
                    } catch (InterruptedException | ExecutionException e) {
                        result =  -1;
                    }
                    if (result == 1) {
                        enable(true);
                        NEW.setVisible(false);
                        boardMsg.setText("Create a new whiteboard. Enter a name and dimensions.");
                        NEW.pack();
                        NEW.setLocationRelativeTo(null);
                    }
                }
            };
            
            worker.execute();
        }
        else if (source == chatTxt) {
            // in here, the user sends chat messages to fellow users
            SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
                @Override
                public Object doInBackground() {
                    String message = chatTxt.getText();
                    if (!(message.equals(""))) {
                        canvas.sendChat(message);
                        chatTxt.setText("");
                        chatPane.getVerticalScrollBar().setValue(chatPane.getVerticalScrollBar().getMaximum());
                    }
                    return 1;
                }
            };
            
            worker.execute();
        }
        else if (source == yesBtn || source == noBtn) {
            // in here, the user can choose whether to overwrite a file or not
            if (source == yesBtn) {
                canvas.saveImage(chooser.getSelectedFile(), true);
            }
            SAVE.setVisible(false);
            this.enable(true);
        }
    }
    
    /**
     * This method simply sets the color and stroke the the client when it is
     * detected that either has changed.
     * 
     * @param e the event that triggered the listener
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        canvas.setCanvasColor(palette.getColor());
        canvas.setCanvasStrokeWidth(strokeSizeSldr.getValue());
    }

    /**
     * This method is called whenever the user wishes to switch to another board.
     * 
     * @param e the event that triggered the listener
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {

        SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
            @Override
            public Object doInBackground() {
                ListSelectionModel model = (ListSelectionModel) e.getSource();
                
                String selection;
                
                if (!model.isSelectionEmpty() && !model.getValueIsAdjusting()) {
                    int selectedIndex = model.getMinSelectionIndex();
                    selection = (String) boards.getValueAt(selectedIndex, 0);
                    canvas.userChangeBoard(selection);
                    WHITEBOARD.setTitle("Whiteboard - " + selection);
                    WHITEBOARD.pack();
                }
                return 1;
            }
        };
        
        worker.execute();
    }

    /**
     * Unimplemented
     */
    @Override
    public void windowActivated(WindowEvent e) {
        
    }

    /**
     * Unimplemented
     */
    @Override
    public void windowClosed(WindowEvent e) {
    }
    
    /**
     * This function is called whenever the user chooses to opt out of
     * changing their username, creating a board, or saving. In such cases,
     * we want to re-enable the application so they may continue.
     * 
     * @param e the event that triggered the listener
     */
    @Override
    public void windowClosing(WindowEvent e) {
        JFrame source = (JFrame) e.getSource();
        if (source == NAME || source == NEW || source == SAVE) {
            this.enable(true);
        }
    }

    /**
     * Unimplemented
     */
    @Override
    public void windowDeactivated(WindowEvent e) {
        
    }

    /**
     * Unimplemented
     */
    @Override
    public void windowDeiconified(WindowEvent e) {
        
    }

    /**
     * Unimplemented
     */
    @Override
    public void windowIconified(WindowEvent e) {
        
    }

    /**
     * Unimplemented
     */
    @Override
    public void windowOpened(WindowEvent e) {
        
    }
    
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MasterGUI main = new MasterGUI();
                main.startup();
            }
        });
    }
}
