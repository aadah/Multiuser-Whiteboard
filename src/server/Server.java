package server;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class acts as a server for the collaborative
 * whiteboard system. The server stores all of the
 * information for all whiteboards and users, and
 * interacts with the users to keep all users
 * updated about the whiteboard. This is a
 * threadsafe class. See documentation
 * below for threadsafety details.
 */
public class Server {
    /*
     * This class stores all of the data about the users
     * and boards of the whiteboard system. The rep 
     * invariants are:
     *  - None of the five hash maps are ever null
     *  - boards, chats, boardEdits, and usersOnBoard
     *    are always non-empty (they have at least
     *    one key-value pair). Namely, they each
     *    have INITIAL_BOARD_NAME as a key.
     *  - usedIDUpTo is always a non-negative int, and
     *    no two users have the same user ID
     *    
     * The following are the abstraction functions:
     *  - A user is in the system if and only if
     *    the userID/username pair are in the users map,
     *    where userID is the client's unique ID assigned
     *    by the server, and username is a not-necessarily
     *    unique visible username for the user
     *  - A board is in the system if and only if
     *    the boardname/BufferedImage pair (the visible
     *    board name, and the board's representative
     *    skeletal image, respectively) are in the
     *    boards map. The BufferedImage
     *    has a width and height and is painted
     *    white, but itself stores no other data
     *    about the image/whiteboard that the clients
     *    see on their local whiteboards.
     *  - Assuming that a given user and board exist:
     *    A user is on a certain board if and only if
     *    the ArrayList<String> stored under key boardname
     *    (of the board) in usersOnBoard contains the ID
     *    of the user
     *  - boardEdits stores an ordered listing (as Strings)
     *    of all edits made to some board in the given
     *    order, by mapping the unique board names to 
     *    ArrayList<String>'s. This correctly defines
     *    a client's visible whiteboard.
     *  - A chat message exists in the system on a certain board
     *    (assuming the board exists) if and only if
     *    the ArrayList<String> stored under key boardname
     *    (of the board) in chats contains the specified
     *    chat message. The ArrayList<String> is an ordered
     *    history of all chat messages on that board.
     *  - A PrintWriter device for a given client
     *    (specifically, for the socket for the
     *    given client) exists in outs (value under
     *    the key userID) if the user--with ID
     *    userID--exists in the system, or has ever
     *    existed in the system. (PrintWriters are not
     *    deleted from this map.)
     * 
     * This class is a threadsafe class.
     * - To read or modify usedIDUpTo, usedIDUpTo is
     *   always locked on first.
     * - port and serverSocket are final, and are never
     *   modified
     * - all other non-hashmap class variables are static,
     *   final, and immutable
     * - all elements of hash maps are only ever used or
     *   modified when they're locked
     * - lock acquisitions never result in deadlock: There are
     *   only two times in this code where 2 locks must be
     *   acquired simultaneously. The first is when
     *   some method locks on a hashmap and then
     *   has to lock on a PrintWriter within that.
     *   The lock on the PrintWriter is
     *   always acquired last, and a thread never needs
     *   to acquire a lock from within a PrintWriter
     *   lock, so no deadlock can occur. The second is
     *   in send(.), where a lock on users must be obtained
     *   before obtaining a lock on boards. Similarly, this
     *   acquisition can be represented always as a DAG and
     *   hence there is no deadlock.
     */
    private final int port;
    private final ServerSocket serverSocket;
    private Integer usedIDUpTo = 0;

    private final String INITIAL_BOARD_NAME = "IHTFP";
    private final static int INIT_WIDTH = 640, INIT_HEIGHT = 480;
    private final static String
        USERCHANGENAME = "USERCHANGENAME",
        USERCHANGEBOARD = "USERCHANGEBOARD",
        ADDBOARD = "ADDBOARD",
        DRAW = "DRAW",
        REMOVEUSER = "REMOVEUSER",
        ADDUSER = "ADDUSER",
        CHAT = "CHAT";

    //Map from board names to arraylist of user ID's on that board
    private final HashMap<String, ArrayList<Integer>> usersOnBoard = new HashMap<String, ArrayList<Integer>>();
    //Map from board names to a skeleton of its image. (Nothing is drawn on the board.)
    private final HashMap<String, BufferedImage> boards = new HashMap<String, BufferedImage>();
    //Map from user ID to username
    private final HashMap<Integer, String> users = new HashMap<Integer, String>();
    //Map from user ID to its PrintWriter. No pairings are ever deleted from this.
    private final HashMap<Integer, PrintWriter> outs = new HashMap<Integer, PrintWriter>();
    //Map from board name to list of edits
    private final HashMap<String, ArrayList<String>> boardEdits = new HashMap<String, ArrayList<String>>();
    //Map from board name to chat history
    private final HashMap<String, ArrayList<String>> chats = new HashMap<String, ArrayList<String>>();

    /**
     * Constructs a new Server given a port number.
     * 
     * @param serverPort The port through which the server connects
     * @throws IOException If the server crashes unexpectedly
     */
    public Server(int serverPort) throws IOException {
        //Create the "default" board
        makeBoardAndInitialize(INITIAL_BOARD_NAME, INIT_WIDTH, INIT_HEIGHT);

        port = serverPort;
        serverSocket = new ServerSocket(port);
    }

    /**
     * Activates the server. The server listens for incoming
     * connections from clients, and makes a connection
     * when a client is found.
     * 
     * @throws IOException If handle connection crashed unexpectedly
     */
    public void serve() throws IOException {
        System.out.println("Server is now running. Server IP address is : " + getServerIP());

        while (true) {
            //Blocks until a client connects
            final Socket socket = serverSocket.accept();
            //Assigns a permanent user ID to the client
            final int userID = nextUserID();

            Thread socketThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        handleConnection(socket, userID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
            socketThread.start();
        }
    }

    /**
     * Handles a connection with a certain client. This
     * method is handled in a background thread, so all
     * active clients (active sockets) are running this
     * method effectively simultaneously, in each of
     * their respective threads.
     * 
     * @param socket The Socket instance connecting the server to this client
     * @param userID The user ID of this particular client
     * @throws IOException If something really goes wrong.
     */
    private void handleConnection(Socket socket, final Integer userID) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        //Adds the PrintWriter to the map so we can access it from outside of this method
        synchronized(outs) { outs.put(userID, out); }

        //Initializes all relevant data for a new user. This only happens once.
        newUser(userID, out);

        //Listen for messages from client:
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                //Determines what type of message the message is, and handles it:
                if(isMessage(line, USERCHANGENAME)) {
                    command_userChangeName(line, userID);
                } else if(isMessage(line, USERCHANGEBOARD)) {
                    command_userChangeBoard(line, userID);
                } else if(isMessage(line, ADDBOARD)) {
                    command_addBoard(line, userID);
                } else if(isMessage(line, DRAW)) {
                    command_draw(line, userID);
                } else if(isMessage(line, CHAT)) {
                    command_chat(line, userID);
                } else {
                    //Invalid command from client. See Line Protocol!;
                    ;
                }
            }
        } catch(SocketException e) {
            //This is typically called when client disconnects
            ;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Close the BufferedReader and PrintWriter
            out.close();
            in.close();
            //Remove the user from the system
            removeUser(userID);
        }
    }
    
    /**
     * Command from the user: change the user's name. See
     * Line Protocol document for details about the message.
     * 
     * Requires: That the message is of the form
     * USERCHANGENAME. That the new username is unique from
     * all other usernames, non-null. That the user
     * whose ID is given is a member of the system.
     * non-empty, and contains no spaces.
     * Effects: The user's username is switched, and
     * all relevant users are informed of this.
     * 
     * @param line The message from the client
     * @param ID The user ID of the client who is changing its name
     */
    private void command_userChangeName(String line, int ID) {
        String[] s = line.split(" ");
        //Replaces the stored username in the map with the new value
        synchronized(users) { users.put(ID, s[1]); }

        //Locates the board that the user is on
        String currentBoard = "";
        synchronized(usersOnBoard) {
            for(String board : usersOnBoard.keySet()) {
                if(usersOnBoard.get(board).contains(ID)) {
                    currentBoard = board;
                    break;
                }
            }
        }

        //Update clients about the change
        pushToClients(USERCHANGENAME, ID, currentBoard, "");
    }

    /**
     * Command from user: change the user's active board. See
     * the Line Protocol document for details about the message.
     * 
     * Requires: That the message be of the form USERCHANGEBOARD.
     * That the board exists. That the user with the given
     * ID is in the system.
     * Effects: The user is moved to the new board, and all
     * relevant users are informed of this.
     * 
     * @param line The message from the user
     * @param ID The user ID of the client who is changing boards.
     */
    private void command_userChangeBoard(String line, int ID) {
        String[] s = line.split(" ");
        String oldBoardName = s[1];
        String newBoardName = s[2];

        //Removes user from old board list and adds to new board list
        synchronized(usersOnBoard) {
            usersOnBoard.get(oldBoardName).remove((Object)ID);
            usersOnBoard.get(newBoardName).add((Integer)ID);
        }

        //Sends the new board data to the user
        sendNewBoardToUser(newBoardName, ID);
        
        //Sends all board chat to the user
        sendChatToUser(newBoardName, ID);

        //Update clients about the change
        pushToClients(USERCHANGEBOARD, ID, oldBoardName, newBoardName);
    }

    /**
     * Command from a client: add a new board. Also moves
     * the user (who added the board) to the new board. See
     * Line Protocol document for details about the message.
     * 
     * Requires: That the message is of the form ADDBOARD.
     * That the user whose ID is given is a member of the
     * system.
     * Effects: The board is created, the user is switched
     * to the new board, and all relevant users are 
     * updated of this.
     * 
     * @param line The message from the user.
     * @param ID The user ID of the client who added the board.
     */
    private void command_addBoard(String line, int ID) {
        String[] s = line.split(" ");

        int width = Integer.parseInt(s[3]);
        int height = Integer.parseInt(s[4]);
        String currentBoardName = s[1];
        String newBoardName = s[2];

        //Creates a new board with the given data
        makeBoardAndInitialize(newBoardName, width, height);

        //Moves the user from the old board to the new board
        synchronized(usersOnBoard) {
            usersOnBoard.get(currentBoardName).remove((Object)ID);
            usersOnBoard.get(newBoardName).add((Integer)ID);
        }

        //Update clients about the change
        pushToClients(ADDBOARD, ID, currentBoardName, newBoardName);

        //Sends the new board data to the given user
        sendNewBoardToUser(newBoardName, ID);
        //Sends board chat history to the given user
        sendChatToUser(newBoardName, ID);
    }
    
    /**
     * Command from client: draw something on a board.
     * The draw is not actually executed on the server's
     * copy of the board. It is only ever distributed
     * to all of the clients.
     * 
     * Requires: That the message is of the form DRAW.
     * That the user whose ID is given is part of
     * the system.
     * Effects: The edit is handled by the server,
     * and the edit is pushed to all relevant users.
     * 
     * @param line The message from the client.
     * @param ID The ID of the user who sent the message
     */
    private void command_draw(String line, int ID) {
        String[] s = line.split(" ");

        //Adds the DRAW instruction to the list of edits
        String boardName = s[2];
        synchronized(boardEdits) { boardEdits.get(boardName).add(line); }

        //Update clients about the change
        pushToClients(DRAW, ID, boardName, line);


    }
    
    /**
     * Command from user: chat. See the Line Protocol document
     * for more details about the message.
     * 
     * Requires: The message be of the form CHAT. The user
     * with the given ID exists in the system.
     * 
     * @param line The CHAT message
     * @param ID The ID of the user calling this
     */
    private void command_chat(String line, int ID) {
        String[] s = line.split(" ");
        String boardName = s[1];
        
        //Adds the chat message to the list of chat messages
        chats.get(boardName).add(line);
        
        //Update clients about the change
        pushToClients(CHAT, ID, boardName, line);
    }

    /**
     * Sends, to a specific user, a message containing
     * all active boards on the system. See Line
     * Protocol document for details about the message
     * sent.
     * 
     * Requires: That the user with the given ID is in the system.
     * That the PrintWriter instance not be closed.
     * Effects: The message with all boards on the system
     * has been sent to the given user.
     * 
     * @param ID The client ID of the user to whom the message is sent
     * @param o The PrintWriter instance of the user to whom the message is sent
     */
    private void sendAllBoardNames(int ID, PrintWriter o) {
        //Creates the message
        String msg = "ALLBOARDS";
        synchronized(boards) {
            for(String boardname : boards.keySet()) {
                msg += (" " + boardname);
            }
        }
    
        //Send the message
        synchronized(o) {
            o.println(msg);
            o.flush();
        }
    }

    /**
     * Sends, to a specific user, a message containing
     * all active users on the system (not just the
     * users on a certain board). See the Line Protocol
     * document for details about the message.
     * 
     * Requires: That the user with the given ID is in the system.
     * That the PrintWriter instance not be closed.
     * Effects: The message with all users on the system
     * has been sent to the given user.
     * 
     * @param ID The client ID of the user to whom the message is sent
     * @param o The PrintWriter instance of the user to whom the message is sent
     */
    private void sendAllUserNames(int ID, PrintWriter o) {
        //Creates the message to send to the user.
        String msg = "ALLUSERS";
        synchronized(users) {
            for(String username : users.values()) {
                msg += (" " + username);
            }
        }
    
        //Sends the message.
        synchronized(o) {
            o.println(msg);
            o.flush();
        }
    }

    /**
     * Sends, to a specific user, a message that tells the user
     * all currently active users (usernames) on a given
     * board. Typically, this method will be used to send
     * info about the users on the given user's same board.
     * See the Line Protocol document for details about the
     * message sent.
     * 
     * Requires: That the board exist. That the user with the
     * given ID is in the system. That the PrintWriter instance
     * not be closed.
     * Effects: The message with all users on the board has
     * been sent to the client in question.
     * 
     * @param ID The client ID of the user to whom the message is sent
     * @param o The PrintWriter instance of the user to whom the message is sent
     * @param board The board name to retrieve all usernames on that board
     */
    private void sendUsersOnBoard(int ID, PrintWriter o, String board) {
        //Creates the message
        String msg = "USERSONBOARD " + board;
        synchronized(usersOnBoard) {
            for(Integer user : usersOnBoard.get(board)) {
                msg += (" " + users.get(user));
            }
        }
    
        //Sends the message
        synchronized(o) {
            o.println(msg);
            o.flush(); 
        }
    }

    /**
     * Sends data about a new board to a specific user. In
     * particular, this method is called when a client first
     * connects (sends the data about the default whiteboard),
     * as well as when a client switches boards (sends the
     * data about the new board). See Line Protocol for 
     * details on the messages sent.
     * 
     * Requires: That the board exist, and the user exists.
     * Effects: The board data is sent to the user.
     * 
     * @param boardName The board name of the board to send the data about
     * @param ID The ID of the user who is being sent the message
     */
    private void sendNewBoardToUser(String boardName, int ID) {
        PrintWriter o = outs.get(ID);
    
        int width, height;
        synchronized(boards) {
            width = boards.get(boardName).getWidth();
            height = boards.get(boardName).getHeight();
        }
    
        //Sends initial message to the client
        synchronized(o) {
            String msg = "NEWBOARD " + boardName + " " + width + " " + height;
            o.println(msg);
            o.flush();
        }
    
        //Sends list of board edits for this board to the client
        synchronized(boardEdits) {
            ArrayList<String> edits = boardEdits.get(boardName);
            int numEdits = edits.size();
            for(int i = 0; i < numEdits; i++) {
                synchronized(o) {
                    o.println(edits.get(i));
                    o.flush();
                }
            }
        }
    
        //Sends closing command to client
        synchronized(o) {
            o.println("ENDEDITS");
            o.flush();
        }
    }

    /**
     * Sends a history of all chat to some user. See Line Protocol
     * document for details about the each message.
     * 
     * Requires: That the board exist. That the user whose
     * ID is given exists.
     * Effects: The list of all chats has been sent to the user.
     * 
     * @param boardName The name of the board in question
     * @param ID The ID of the particular user.
     */
    private void sendChatToUser(String boardName, int ID) {
        PrintWriter o = outs.get(ID);

        //Sends list of board edits for this board to the client
        synchronized(chats) {
            ArrayList<String> allChats = chats.get(boardName);
            int numChats = allChats.size();
            for(int i = 0; i < numChats; i++) {
                synchronized(o) {
                    o.println(allChats.get(i));
                    o.flush();
                }
            }
        }
    }

    /**
     * Sends an edit message to a specific user. See the
     * Line Protocol document for details about the
     * message.
     * 
     * Requires: That the message be a valid DRAW-form message.
     * That the user with the given ID is in the system. That
     * the PrintWriter instance not be closed.
     * Effects: The edit message is sent to the user.
     * 
     * @param ID The ID of the recipient user
     * @param o The PrintWriter instance
     * @param msg The DRAW message to send
     */
    private void sendEditMessage(int ID, PrintWriter o, String msg) {
        synchronized(o) {
            o.println(msg);
            o.flush();
        }
    }
    
    /**
     * Pushes relevant updates to relevant users. In other words,
     * if some update is made (board change, new user, new draw, 
     * etc.), each client that is affected is notified of this.
     * 
     * Requires: That the relevant message from the client has
     * already been processed appropriately server-side. That
     * the flag type is correct. That callerID is a valid
     * user in the system.
     * 
     * @param flag The style of message/event that occurred
     * @param callerID The ID of the user who is the original caller of this method
     * @param board1 The relevant board name, if applicable.
     * @param board2 Another relevant board name, if applicable.
     * @throws RuntimeException If a flag is not of the right form
     */
    private void pushToClients(String flag, int callerID, String board1, String board2) {
        if(flag.equals(ADDUSER) || flag.equals(REMOVEUSER) || flag.equals(USERCHANGENAME)) {
            //Send list of all users to all users
            send("users", "all");
            //Send new board user list to correct board
            send("usersonboard", board1);
        } else if(flag.equals(USERCHANGEBOARD)) {
            //Send new board user list to old board
            send("usersonboard", board1);
            //Send new board user list to new board
            send("usersonboard", board2);
        } else if(flag.equals(ADDBOARD)) {
            //Send list of all boards to all users
            send("boards", "all");
            //Send new board user list to old board
            send("usersonboard", board1);
            //Send new board user list to new board
            send("usersonboard", board2);
        } else if(flag.equals(DRAW)) {
            //Send edit message to users on board
            String stringMessage = board2;
            send("edit", stringMessage);
        } else if(flag.equals(CHAT)) {
            //Send chat message to all users on board
            String stringMessage = board2;
            send("chat", stringMessage);
        } else {
            throw new RuntimeException("Unknown flag type. What did you do!?");
        }
    }

    /**
     * Sends a certain message to a certain set of recipients.
     * This method is called from pushToClients to make
     * handling the data a bit easier. See Line Protocol
     * document for details about the messages sent to 
     * the clients.
     * 
     * Requires: That the message and recipients are of the
     * appropriate type.
     * Effects: The message has been sent to the appropriate
     * clients.
     * 
     * @param message The type of message to send to recipients
     * @param recipients The recipients to receive the message
     */
    private void send(String message, String recipients) {
        //Send a list of all users
        if(message.equals("users")) { 
            synchronized(users) {
                for(int userID : users.keySet()) {
                    PrintWriter o = outs.get(userID);
                    sendAllUserNames(userID, o);
                }
            }
        }
        //Send a list of all boards
        else if(message.equals("boards")) { 
            synchronized(users) {
                for(int userID : users.keySet()) {
                    PrintWriter o = outs.get(userID);
                    sendAllBoardNames(userID, o);
                }
            }
        }
        //Send a list of users on a certain board
        else if(message.equals("usersonboard")) { 
            synchronized(usersOnBoard) {
                for(int userID : usersOnBoard.get(recipients)) {
                    PrintWriter o = outs.get(userID);
                    sendUsersOnBoard(userID, o, recipients);
                }
            }
        }
        //Send an edit message
        else if(message.equals("edit")) {

            String[] s = recipients.split(" ");
            String boardName = s[2];
            synchronized(usersOnBoard) {
                for(Integer userID : usersOnBoard.get(boardName)) {
                    PrintWriter o = outs.get(userID);
                    sendEditMessage(userID, o, recipients);
                }
            }
        }
        //Send a chat message
        else if(message.equals("chat")) {
            String[] s = recipients.split(" ");
            String boardName = s[1];
            synchronized(usersOnBoard) {
                for(Integer userID : usersOnBoard.get(boardName)) {
                    PrintWriter o = outs.get(userID);
                    sendEditMessage(userID, o, recipients);
                }
            }
        }
        //Wrong flag type
        else {
            throw new RuntimeException("Not a valid \"send\" message!");
        }

    }

    /**
     * Makes a new board (typically as a result from a NEWBOARD
     * message from a client, OR when the server is started)
     * given the name of the board, its width, and its height.
     * 
     * Requires: That the name of the board is unique (that no 
     * other board has this name), non-null, non-empty, and
     * contains no spaces. That the width and height are
     * positive ints.
     * Effects: The board is created, stored, and added to all
     * appropriate HashMaps.
     * 
     * @param name The name of the new board
     * @param w The width of the new board
     * @param h The height of the new board
     */
    private void makeBoardAndInitialize(String name, int w, int h) {
        BufferedImage newBoard = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gImage = (Graphics2D)newBoard.getGraphics();
        gImage.setColor(Color.WHITE);
        gImage.fillRect(0, 0, w, h);
    
        //Adds the new board to all relevant maps
        synchronized(boards) { boards.put(name, newBoard); }
        synchronized(usersOnBoard) { usersOnBoard.put(name, new ArrayList<Integer>()); }
        synchronized(boardEdits) { boardEdits.put(name, new ArrayList<String>()); }
        synchronized(chats) { chats.put(name, new ArrayList<String>()); }
    }

    /**
     * Called when a client first connects to the server.
     * This method sends the board data to the client, then
     * sends the list of all board names, then sends a 
     * list of all users.
     * 
     * Requires: That the PrintWriter instance has already
     * been defined and added to the map of outs.
     * That the ID has been appropriately
     * defined and assigned. That at least one board
     * exists (the default board).
     * 
     * Effects: The client has been sent its board data,
     * a list of all boards, and a list of all users.
     * All users are informed of the new user, and 
     * users on the current board are informed that
     * the new user has connected to this board.
     * 
     * @param ID The ID of the newly-connecting user
     * @param o The PrintWriter instance
     */
    private void newUser(int ID, PrintWriter o) {
        //Add user to the correct maps
        synchronized(users) { users.put(ID, validNewUserName()); }
        synchronized(usersOnBoard) { usersOnBoard.get(INITIAL_BOARD_NAME).add(ID); }
    
        //Sends board data, all board names, all user names; to the client
        sendNewBoardToUser(INITIAL_BOARD_NAME, ID);
        sendChatToUser(INITIAL_BOARD_NAME, ID);
        sendAllBoardNames(ID, o);
        sendAllUserNames(ID, o);
    
        //Update clients about the change        
        pushToClients(ADDUSER, ID, INITIAL_BOARD_NAME, "");
    }

    /**
     * Called when a client disconnects. This removes the
     * user from the system.
     * 
     * Requires: That the user originally be a valid user
     * in the system. That the socket has just been
     * disconnected.
     * Effects: The user has been removed from all records,
     * and all other relevant users are informed appropriately.
     * The user's PrintWriter will NOT have been removed
     * from the PrintWriter map.
     * 
     * @param ID The ID of the user who is being removed.
     */
    private void removeUser(int ID) {
        //Removes user from users map.
        synchronized(users) { users.remove(ID); }
    
        //Removes user from usersOnBoard map.
        String board = "";
        synchronized(usersOnBoard) {
            for(String boardName : usersOnBoard.keySet()) {
                if(usersOnBoard.get(boardName).contains(ID)) {
                    board = boardName;
                    usersOnBoard.get(boardName).remove((Object)ID);
                    break;
                }
            }
        }
    
        //Update clients about the change
        pushToClients(REMOVEUSER, ID, board, "");
    }

    /**
     * Checks if a string is of a certain form with regards
     * to the server-client socket line protocol. See Line
     * Protocol document for details.
     * 
     * @param s The message to check
     * @param pattern The pattern (form) to check against
     * @return True if the message is of the given form
     */
    protected boolean isMessage(String s, String pattern) {
        return s.startsWith(pattern);
    }

    /**
     * Gets the next valid user ID to assign
     * to an incoming client who is connecting
     * to the server.
     * 
     * @return The ID to be assigned to a client.
     */
    protected Integer nextUserID() {
        //We lock on usedIDUpTo to ensure that IDs given are unique
        int ID;
        synchronized(usedIDUpTo) {
            usedIDUpTo++;
            ID = usedIDUpTo;
        }
    
        return ID;
    }
    
    /**
     * Finds a valid "temporary" user name for a 
     * newly-connected user, of the form newuser<x>
     * where <x> is a non-negative integer.
     */
    protected String validNewUserName() {
        String base = "newuser";
        int number = 0;

        //Looks through users to see what usernames are available
        synchronized(users) {
            while(true) {
                if(users.values().contains(base + number)) {
                    number++;
                } else {
                    break;
                }
            }
        }
        return (base + number);
    }

    /**
     * Gets the IP address of the server. Typically
     * used for debugging or for testing purposes.
     * 
     * Requires: That the server have a valid internet
     * connection. That either Didit or Google is up
     * and running.
     * 
     * @return The IPv4 address of the running server--or "N/A" if no network access.
     */
    protected String getServerIP() {
        Socket s;
        String IP = "";
    
        /*
         * Attempts to open a socket with a website so
         * that we can take the address of the local
         * side of the socket. Note that this tries to
         * connect to Didit; then to Google. If both
         * fail, sets the IP to "N/A".
         */
        try {
            s = new Socket("didit.csail.mit.edu", 80);
            IP = s.getLocalAddress().getHostAddress();
            s.close();
        } catch(IOException e) {
            try {
                s = new Socket("google.com", 80);
                IP = s.getLocalAddress().getHostAddress();
                s.close();
            } catch(IOException f) {
                IP = "N/A";
            }
        }
    
        return IP;
    }

    /**
     * Runs the server.
     * 
     * @param args String args in Java init call
     * @throws IOException if server can't connect for some reason
     */
    public static void main(String[] args) throws IOException {
        Server server = new Server(4444);
        server.serve();
    }

}
