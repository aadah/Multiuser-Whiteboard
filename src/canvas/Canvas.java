package canvas;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Canvas contains the Image that is the representation of the board on which users
 * 	can make free-hand drawings, line segments, and erase free-hand. It also contains
 * 	all the methods that can be called by the GUI to send messages containing
 * 	information defining new Edits, changing the board, adding a new board, or changing
 *  the username to the server, as well as handling messages from the server
 * 	to change the board image, or update the table models and lists of users and boards.
 * Rep Invariant:
 * 	currentUser and currentBoard are not null
 * 	out is not null
 * 	No two values in usersOnBoard or allUserNames can be the same
 * 	No two values in boardNames can be the same
 * Thread safety argument:
 * 	The main thread only takes in mouseEvents and sends messages to the server,
 * 	while the thread responsible for maintaining the socket connection with the
 *  server makes all the calls to methods that change the board, the table
 *  models and the lists storing the usernames and boardnames. 
 *  This means that no two methods can possibly modify or access the same objects
 *  concurrently. The scope of each thread prevents it from occurring.
 *  The exception is the case where edits are made to the board while segmentDrawing 
 *  is true and the segmentEditQ queue is being emptied between mouse drag events. 
 *  This is thread-safe because all edits received from the server go into the queue
 *  while segmentDrawing is true, which means that it is not attempting to edit the
 *  board and only the main thread is doing so until the mouse is released, at which
 *  point segmentDrawing is set to false and the thread with the socket connection
 *  is the only thing editing the board again.
 *  At any point in time, only one of the two boards can possibly edit the board.
 * 	
 */
public class Canvas extends JPanel {
    // Image where the user's drawing is stored
    private Image drawingBuffer;
    
    // Image for storing the latest image while temporary edits to
    // drawingBuffer is being made so that they can be painted
    // over and the original image is preserved
    private Image snapShot;
    
    private int imWidth;
    private int imHeight;
    
    private String currentBoard; // name of current board
    private String currentUser; // current user's username
    
    // DrawingMode can take four values: "pencil", "segment", "erase", and "fill"
    // (Note: "fill" though implemented, is not yet available for user access
    // due to insufficient testing)
    private String drawingMode;
    // Flag for whether or not user is erasing
    private boolean erasing; 
    // Flag for whether a segment is being dragged out but not yet finalized
    // with a mouse release
    private boolean drawingSegment;
    // Flag for whether board is still in the process for updating a new board
    // with all the previous edits that were made on it to bring it up to date
    // to ensure that it is displaying the latest image
    private boolean initializing; 
    
    private Color color;
    private Color lastColor;
    private Stroke stroke;
    
    // If a segment is still being drawn out, but not yet finalized with a mouse
    // release, Edits passed in from the server go into segmentEditQ so that in
    // between drags, and after the segment has been finalized, the Edits can be 
    // updated from this queue, and after the segment has been finalized, Edits 
    // that were held up can be updated
    private ArrayList<Edit> segmentEditQ;
    
    private int portNumber;
    
    private PrintWriter out;
    
    private ArrayList<String> usersOnBoard;
    private ArrayList<String> allUserNames;
    private ArrayList<String> boardNames;
    
    private HashMap<String, Dimension> boardDatum;
    
    // DefaultTableModels to which the GUI can get direct access
    // for automatically displaying updated lists when data is 
    // received from server
    private DefaultTableModel usersTableModel;
    private DefaultTableModel boardsTableModel;
    private DefaultTableModel chatTableModel;
    
    /**
     * Make a canvas.
     */
    public Canvas() {
        addDrawingController();
        this.setFocusable(true);
        // note: we can't call makeDrawingBuffer here, because it only
        // works *after* this canvas has been added to a window.  Have to
        // wait until paintComponent() is first called.
        
        this.currentUser = "";
        this.currentBoard = "";
        
        this.drawingMode = "pencil";
        this.erasing = false;
        this.drawingSegment = false;
        
        this.color = Color.BLACK;
        // Initializes stroke to a BasicStroke with parameters
        // width = 1, cap = 1 (CAP_ROUND), and join = 1 (JOIN_ROUND)
        this.stroke = new BasicStroke(1,1,1);
        this.lastColor = Color.BLACK;
        
        this.segmentEditQ = new ArrayList<Edit>();
        
        this.usersOnBoard = new ArrayList<String>();
        this.allUserNames = new ArrayList<String>();
        this.boardNames = new ArrayList<String>();
        
        this.boardDatum = new HashMap<String, Dimension>();
        
        this.portNumber = 4444;
        
        // In-line class initialization for the DefaultTableModels
        // in order to override isCellEditable method which is necessary
        // for preventing users from directly editing cell data in the GUI
        this.usersTableModel = new DefaultTableModel(new String[] {"Users on Board"}, 0) {
        	@Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }	
        };
        this.boardsTableModel = new DefaultTableModel(new String[] {"Boards"}, 0) {
        	@Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }	
        };
        this.chatTableModel = new DefaultTableModel(new String[] {"User","Time","Message"}, 0) {
        	@Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }	
        };	
    }
    
    /**
     * Connects to server on a new thread.
     * Handles each type of String input from server in the same thread by 
     * directing the provided input parameters to relevant methods for updating
     * the board, as well as lists of users on the board, users, and boards 
     * @param customIP
     */
    public void connectToServer(final String customIP){
    	Runnable serverConnection = new Runnable() {
        	public void run() {
        		try {
        			Socket client = new Socket(InetAddress.getByName(customIP), portNumber);
        			
        			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        			out = new PrintWriter(client.getOutputStream(), true);
        			String line = "";
        	        while ((line = in.readLine()) != null){
        	        	String[] inputs = line.split(" ");
        	        	String command = inputs[0];
        				switch (command){
        					case "NEWBOARD":
        						handleNewBoard(inputs);
        						chatTableModel.setRowCount(0);
        						initializing = true;
        						break;
        					case "ENDEDITS":
        						initializing = false;
        						break;
        					case "CHAT":
        						receiveChat(line);
        						break;
        					case "DRAW":
        						String editType = inputs[1];
        						switch (editType){
        							case "SEGMENT":
		        						SegmentEdit segEd = inputToSegmentEdit(inputs);
		        						receiveUpdate(segEd,drawingSegment);
		        						break;
        							case "FILL":
        								final FillEdit fillEd = inputToFillEdit(inputs);
        								Runnable fillOp = new Runnable(){
            								public void run(){
            									receiveUpdate(fillEd,drawingSegment);
            								}
        								};
        								Thread fillThread = new Thread(fillOp);
        								fillThread.start();
        								break;
        							default:
        								break;
        						}
        						break;
        					case "USERSONBOARD":
        						assert(inputs[1].equals(currentBoard));
    							usersOnBoard.clear();
        						usersTableModel.setRowCount(0);
        						for(int i = 2; i < inputs.length; i++){
        							usersOnBoard.add(inputs[i]);
            			    		usersTableModel.addRow(new String[] {inputs[i]});
        						}
        						break;
        					case "ALLUSERS":
    							allUserNames.clear();
        						for(int i = 1; i < inputs.length; i++){
        							allUserNames.add(inputs[i]);
        						}
        						break;
        					case "ALLBOARDS":
    							boardNames.clear();
    							boardsTableModel.setRowCount(0);
        						for(int i = 1; i < inputs.length; i++){
        							boardNames.add(inputs[i]);
            						boardsTableModel.addRow(new String[] {inputs[i]});
        						}
        						break;
        					default:
        		                break;
        				}
        	        }
        			client.close();
        		} catch (IOException e) {}
        	}
        };
        Thread t1 = new Thread(serverConnection);
        t1.start();
    }
    
    /**
     * Determines if a file already exists at the given location by
     * trying to read the file
     * @param file File being checked for existence
     * @return boolean for whether or not a file already exists
     */
    public boolean existsAlready(File file){
		try{
			ImageIO.read(file);
		}catch(IOException ex){
			return false;
		}
		// If it was possible to read file, then file must already
		// exist, so we return true
		return true;
	}


	/**
     * Saves image to a file location with a valid extension
     * 	If file already exists at location, and hard is false,
     * 	we return false so file is not saved.
     * 	If file already exists at location, and hard is true,
     * 	we proceed to save the file.
     * @param file File where the user would like to save the image
     * @param hard boolean for whether existing version should be overwritten
     * in the event that another file of the same name already exists
     * @return boolean for whether or not the file already exists
     */
    public boolean saveImage(File file, boolean hard){
    	// Make a deep copy of the image at the moment when saveImage is called
    	// so that the saved image is the one displayed when the user presses save
    	// instead one containing edits made while the image being saved as well.
    	RenderedImage toBeSaved = deepCopy((BufferedImage)drawingBuffer);
    	try{
    		String path = file.getAbsolutePath();
    		String format = "png"; // default format
    		String[] allowed = 
    				new String[] {".jpg",".JPG",".jpeg",".JPEG",".gif",".GIF",".png",".PNG"};
    		if(format.indexOf(".") > 0){
    			String possibleExtension = path.substring(format.lastIndexOf("."));
    			if(new ArrayList<String>(Arrays.asList(allowed)).contains(possibleExtension)){
    				format = possibleExtension;
    			}else{
    				// If original file lacks a valid extension, append .png
    				file = new File(path + "." + format);
    			}
    		}else{
    			// If original file lacks a valid extension, append .png
    			file = new File(path + "." + format);
    		}
    		if(!hard && existsAlready(file)){
    			return false;
    		}
    		ImageIO.write(toBeSaved, format, file);
    		return true;
    	}catch (IOException ex){
    	}
		return true;
    }
    
    /**
     * Converts array of parameters provided by server into a SegmentEdit object
     * @param inputs a String array of inputs created from input String sent by server
     * @return SegmentEdit an object created from the parameters in inputs array
     */
    public SegmentEdit inputToSegmentEdit(String[] inputs){
		int n = 2; // To account for DRAW, SEGMENT
		String editBoard = inputs[n];
		String editUser = inputs[n+1];
		int x1 = Integer.parseInt(inputs[n+2]);
		int y1 = Integer.parseInt(inputs[n+3]);
		int x2 = Integer.parseInt(inputs[n+4]);
		int y2 = Integer.parseInt(inputs[n+5]);
		Color color = Color.decode(inputs[n+6]);
		int w = Integer.parseInt(inputs[n+7]);
		SegmentEdit edit = new SegmentEdit(editBoard, editUser, x1, y1, x2, y2, color, w);
		return edit;
	}

    /**
     * Converts array of parameters provided by server into a FillEdit object
     * @param inputs a String array of inputs created from input String sent by server
     * @return FillEdit an object created from the parameters in inputs array
     */
	public FillEdit inputToFillEdit(String[] inputs){
		int n = 2; // To account for DRAW, FILL
		String editBoard = inputs[n];
		String editUser = inputs[n+1];
		int x = Integer.parseInt(inputs[n+2]);
		int y = Integer.parseInt(inputs[n+3]);
		Color color = Color.decode(inputs[n+4]);
		FillEdit edit = new FillEdit(editBoard, editUser, x, y, color);
		return edit;
	}

	/**
	 * Handles an edit produced by the parameters sent from the server
	 * 	If the edit is asked to be handled while drawingSegment is true
	 * 	it is placed onto the queue segmentEditQ
	 * 	Otherwise, it is applied to the drawingBuffer as defined by the 
	 * 	edit's apply() method
	 * @param edit an Edit object to be handled
	 * @param drawingSegment a boolean 
	 * @return
	 */
	public boolean receiveUpdate(Edit edit, boolean drawingSegment){
    	if(drawingSegment){
    		segmentEditQ.add(edit);
    	}else{
	    	if(!edit.getBoardID().equals(currentBoard)){
	    		return false;
	    	}
	    	if(edit.isSegmentEdit()){
	    		SegmentEdit segEd = (SegmentEdit) edit;
		    	segEd.apply(drawingBuffer);
		    	this.repaint();
	    	}else if(edit.isFillEdit()){
	    		FillEdit fillEd = (FillEdit) edit;
	    		fillEd.apply(drawingBuffer);
	    		this.repaint();
	    	}
    	}
    	return true;
    }
    
	/**
	 * Sends an edit to the server in the form of a String as defined by the
	 * line protocol
	 * @param edit the Edit object to be serialized and sent to the server
	 */
    public void sendUpdate(Edit edit){
        out.println("DRAW " + edit.serialize());
    }
    
    /**
     * Handles the String containing the parameters defining a chat
     * and updates the chatTableModel to display the parsed information
     * @param line the String from the server containing the information
     * for a chat message in the format defined by the line protocol
     */
    public void receiveChat(String line){
		String[] inputs = line.split(" ");
		String user = inputs[2];
		String time = inputs[3];
		String message = line.substring(line.indexOf(time) + time.length() +1);
		String[] rowData = new String[] {user, time, message};
		chatTableModel.addRow(rowData);
	}

    /**
     * Sends a chat message with the necessary accompanying information
     * such as the board, user, current time, and the contents of the message
     * @param message the String message submitted by the user
     */
	public void sendChat(String message){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String time = sdf.format(cal.getTime()).toString();
		out.println("CHAT " + currentBoard + " " + currentUser + " " + 
					time + " " + message);
	}

	/**
	 * Checks that no board already has the name submitted
	 * Precondition: boardName must not have spaces
	 * @param boardName the String name for the board that is being validated
	 * @return a boolean for whether or not the name is valid, that is,
	 * whether or not another board already has the name
	 */
	public boolean validateNewBoardName(String boardName){
		return !boardNames.contains(boardName);
	}

	/**
	 * Handles the String array of input parameters extracted from the String
	 * provided by the server in the order defined by the line protocol
	 * @param inputs a String array containing all the necessary information
	 * for defining a new Image, which is the representation of board
	 */
	public void handleNewBoard(String[] inputs){
		currentBoard = inputs[1];
		imWidth = Integer.parseInt(inputs[2]);
		imHeight = Integer.parseInt(inputs[3]);
		makeDrawingBuffer(imWidth, imHeight);
		boardDatum.put(currentBoard, new Dimension(imWidth,imHeight));
		setPreferredSize(new Dimension(imWidth, imHeight));
	}

	/**
	 * If newBoardName is valid, sends a message to server containing the 
	 * necessary information for creating a new board, such as the current
	 * board's name, the new board's name, and the new dimensions
	 * @param newBoardName String name for the new board
	 * @param width integer value defining the width of the new board
	 * @param height integer value defining the height of the new board
	 * @return boolean value for whether the operation was successful
	 */
	public boolean addBoard(String newBoardName, int width, int height){
    	if(validateNewBoardName(newBoardName)){
    		boardDatum.put(newBoardName, new Dimension(width,height));
    		out.println("ADDBOARD " + currentBoard + " " + newBoardName +
    					" " + width + " " + height);
    		return true;
    	}
    	return false;
    }
    
	/**
	 * Sends message to server containing the information for making a switch
	 * to a different, already existing board, such as the current board's name
	 * and the name of the board to be switched to
	 * @param newBoardName String name for the board being switched to
	 * @return boolean value for whether the operation was successful
	 */
    public boolean userChangeBoard(String newBoardName){
		if(boardNames.contains(newBoardName)){
			setPreferredSize(boardDatum.get(newBoardName));
			out.println("USERCHANGEBOARD " + currentBoard + " " + newBoardName);
			return true;
		}
		return false;
	}

    /**
     * Checks if a new userName has already been taken or not
     * @param userName String for the new username being validated
     * @return whether the userName is valid, dependent upon whether
     * another user already has the given username
     */
	public boolean validateUserName(String userName){
		return !allUserNames.contains(userName) || userName.equals(currentUser);
	}

	/**
	 * If newUserName is valid, sends message to server with a new username
	 * for changing the user's name, and update's the current user's name 
	 * @param newUserName String for the user's new username
	 * @return boolean for whether the operation was successful
	 */
	public boolean userChangeName(String newUserName){
    	if(validateUserName(newUserName)){
    		out.println("USERCHANGENAME " + newUserName);
    		currentUser = newUserName;
    		return true;
    	}
    	return false;
    }
    
	/**
	 * Gets the current board's name
	 * @return String for current board's name
	 */
    public String getBoardName(){
		return currentBoard;
	}

    /**
     * Gets the current user's name
     * @return String for the current user's name
     */
	public String getUserName(){
		return currentUser;
	}

	/**
	 * Gets the DefaultTableModel containing the board names
	 * @return the canvas's boardsTableModel
	 */
	public DefaultTableModel getBoardsTableModel(){
		return boardsTableModel;
    }
    
	/**
	 * Gets the DefaultTableModel containing the names of the current
	 * board's users
	 * @return the canvas's userstableModel
	 */
    public DefaultTableModel getUsersTableModel(){
    	return usersTableModel;
    }
    
    /**
     * Gets the DefaultTableModel containing information for each chat
     * on this board, including the user, the time stamp and the message String
     * @return the canvas's chatTableModel
     */
    public DefaultTableModel getChatTableModel(){
    	return chatTableModel;
    }

    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g) {
    	super.paintComponent(g);
        // If this is the first time paintComponent() is being called,
        // make our drawing buffer.
        if (drawingBuffer == null) {
            makeDrawingBuffer(imWidth, imHeight);
        }
        // Copy the drawing buffer to the screen.
        g.drawImage(drawingBuffer, 0, 0, null);
    }
    
    /**
     * Makes the drawing buffer with given width and height, and
     * fills it with white
     * @param width integer value defining width of drawingBuffer
     * @param height integer value defining the height of drawingBuffer
     */
    private void makeDrawingBuffer(int width, int height) {    	
        drawingBuffer = createImage(width, height);
        fillWithWhite();
    }
    
    /**
     * Fills the entire drawingBuffer with white
     */
    private void fillWithWhite() {
        final Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imWidth, imHeight);
        // IMPORTANT!  Every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
    }    
    
    /**
     * Makes a deep copy of the BufferedImage
     * @param im the BufferedImage of which the deep copy is to be made
     * @return a deep copy of the BufferedImage
     */
    private BufferedImage deepCopy(BufferedImage im) {
    	 WritableRaster raster = im.copyData(null);
    	 ColorModel cm = im.getColorModel();
    	 return new BufferedImage(cm, raster, false, null);
    }
    
    /**
     * Creates a SegmentEdit using the parameters that define a line segment, 
     * including its color, start coordinates, and end coordinates, in order
     * to send as an update to the server
     * @param color the Color with which the line segment was drawn
     * @param x1 the x value of the start coordinates
     * @param y1 the y value of the start coordinates
     * @param x2 the x value of the end coordinates
     * @param y2 the y value of the end coordinates
     */
    private void drawLineSegment(Color color, int x1, int y1, int x2, int y2) {
        SegmentEdit edit = new SegmentEdit(currentBoard, currentUser, 
        									x1, y1, x2, y2, color, stroke);
        sendUpdate(edit);
    }
    
    /**
     * Creates a FillEdit using the parameters that define a fill, 
     * including its color, and start coordinates, in order
     * to send as an update to the server 
     * @param color the Color with which the region is to be filled
     * @param x0 the x value of the start coordinates
     * @param y0 the y value of the start coordinates
     */
    private void fillRegion(Color color, int x0, int y0){
    	FillEdit edit = new FillEdit(currentBoard, currentUser, x0, y0, color);
    	sendUpdate(edit);
    }
    
    /**
     * Sets the color to be used for drawing operations by the drawingBuffer's
     * Graphics2D
     * @param c Color that will be used for drawing operations
     */
    public void setCanvasColor(Color c) {
    	// If user tries to change color while in erase mode, store it in
    	// last color and continue in the meantime with white.
    	if(drawingMode.equals("erase")){
        	lastColor = c;
        }else{
        	lastColor = color; // Store current color as last color for easier return
        	color = c;
        }     
    }
    
    /**
     * Sets the width of the stroke for drawing freehand and making
     * segments
     * @param w integer value defining the width of the stroke used
     * for drawing
     */
    public void setCanvasStrokeWidth(int w){
    	stroke = new BasicStroke(w,1,1);
    }
    
    /**
     * Changes the necessary attributes to alter the behaviour of the canvas
     * depending on its current mode, which can be one of the following:
     * "pencil", "segment", "fill", and "erase"
     * @param mode a String corresponding to each possible mode
     */
    public void setCanvasMode(String mode){
    	drawingMode = mode;
    	if(drawingMode.equals("pencil")){
    		if(erasing){
    			erasing = false;
    			color = lastColor;
    		}
    	}else if(drawingMode.equals("segment")){
    		if(erasing){
    			erasing = false;
    			color = lastColor;
    		}
    	}else if(drawingMode.equals("fill")){
    		if(erasing){
    			erasing = false;
    			color = lastColor;
    		}
    	}else if(drawingMode.equals("erase")){
    		erasing = true;
    		lastColor = color;
    		color = Color.WHITE;
    	}
    }
    
    /**
     * Add the mouse listener that supports the user's freehand drawing.
     */
	private void addDrawingController() {
	    DrawingController controller = new DrawingController();
	    addMouseListener(controller);
	    addMouseMotionListener(controller);
	    addKeyListener(controller);
	}

	/**
	 * This class handles the user's mouse actions depending on the mode
	 * 	that the canvas is in.
	 * Thread-safety argument:
	 * 	Since a user can only ever have one mouse, everything occurs serially
	 * 	in this class and no two methods access or modify lastX and lastY
	 * 	simultaneously.
	 */
    private class DrawingController implements MouseListener, MouseMotionListener, KeyListener {
        // Store the coordinates of the last mouse event, so we can
        // draw a line segment from that last point to the point of 
    	// the next mouse event.
        private int lastX, lastY; 

        /**
         * Handles the event of a mousePress
         * 	If the board is still in the initializing stage, nothing
         * 	is done.
         * 	Otherwise, the current coordinates are stored as a reference
         * 	point for any drawing action that requires an initial point
         * Creates deep copy of current image for repainting when a temporary
         * drawing is made (e.g. while the end of a line segment is dragged
         * around and is not yet a permanent part of the image, but needs to
         * be displayed for the user)
         */
        public void mousePressed(MouseEvent e) {
        	if(initializing){
        		// Do nothing if user tries to draw before the board
        		// is finished initializing
        	}else{
	            lastX = e.getX();
	            lastY = e.getY();
	            snapShot = deepCopy((BufferedImage)drawingBuffer);
	            if(drawingMode.equals("segment")){
	            	drawingSegment = true;
	            }
        	}
        }

        /**
         * When mouse is dragged while in "pencil" or "erase" mode, draw each 
         * line segment "differential" to form a free-drawn line.
         * When mouse is dragged while in "segment" mode, show each potential line
         * segment between the start point and the current mouse location.
         */
        public void mouseDragged(MouseEvent e) {
        	int x = e.getX();
            int y = e.getY();
            if(initializing){
            	// Do nothing while still initializing
            }else if(drawingMode.equals("pencil")||drawingMode.equals("erase")){
	            drawLineSegment(color, lastX, lastY, x, y);
	            lastX = x;
	            lastY = y;
        	}else if(drawingMode.equals("segment")){
        		Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();
                g.drawImage(snapShot, 0, 0, null);
                // Updates all edits on the queue
                while(!segmentEditQ.isEmpty()){
                	Edit edit = segmentEditQ.get(0);
                	segmentEditQ.remove(0);
                	receiveUpdate(edit,false);
                }
                // Take a new deep copy of the image after the updates
                // have been made on the image
                // This prevents edits received while a segment is not
            	// yet drawn from being repainted over by the snapShot
            	// each time the mouse is dragged
                snapShot = deepCopy((BufferedImage)drawingBuffer);
        		g.setColor(color);
                g.setStroke(stroke);
                g.drawLine(lastX, lastY, x, y);
                repaint();
        	}
        }
        
        /**
         * When mouse is released in "erase" or "pencil" mode, make last line
         * segment "differential" between the stored coordinates and the
         * coordinates of release
         * When mouse is released in "segment" mode, draw the line segment
         * from the starting coordinates to the coordinates of release, and
         * update all the edits in the segmentEditQ
         * When mouse is in "fill" mode, fill the region containing the
         * coordinates of mouse release
         * Each operation described, other than the edits from segmentEditQ
         * are done indirectly through a message to the server that is returned
         * and triggers the actual edit to be made on the drawingBuffer
         */
		public void mouseReleased(MouseEvent e) {
			int x = e.getX();
            int y = e.getY();
			if(initializing){
				// Do nothing while still initializing
			}else if(drawingMode.equals("erase")){
				drawLineSegment(color, lastX, lastY, x, y);
			}else if(drawingMode.equals("pencil")){
				drawLineSegment(color, lastX, lastY, x, y);
			}else if(drawingMode.equals("segment")){
	            drawingSegment = false;
	            while(!segmentEditQ.isEmpty()){
                	Edit edit = segmentEditQ.get(0);
                	segmentEditQ.remove(0);
                	receiveUpdate(edit,false);
                }
	            drawLineSegment(color, lastX, lastY, x, y);
        	}else if(drawingMode.equals("fill")){
        		fillRegion(color, x, y);
        	}
        }

        // Ignore all these other mouse events.
        public void mouseMoved(MouseEvent e) { }
        public void mouseClicked(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }

		@Override
		public void keyTyped(KeyEvent e) {}
		@Override
		public void keyPressed(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {}
    }
}