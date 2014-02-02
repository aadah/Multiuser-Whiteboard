package server;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Testing suite for integrated test of server-client
 * interaction, for a server with two clients.
 * 
 * IMPORTANT:
 * Pre-condition for running these tests: that no server
 * is already running on this computer.
 * 
 * @category no_didit
 */
public class IntegratedTests {
    /*
     * Testing strategy: test all line protocol methods
     * with both 1 and 2 users.
     * New board, new user, change user name, 
     * change user's board, chat, draw. Each 
     * of these are tested with all reasonable
     * combinations of arguments which are
     * reasonably considered to represent
     * all interactions available, with the
     * exception of high-traffic, high-user
     * systems.
     */
    
    private String serverName = "127.0.0.1";
    private int port = 4444;
    
    /**
     * Tests comprehensive interaction between clients and a server.
     * 
     * IMPORTANT:
     * Pre-condition for running these tests: that no server
     * is already running on this computer.
     * 
     * @throws InterruptedException When thrown from handler
     * @throws IOException When Server throws IOException
     */
    @Test
    public void test_interaction_comprehensive() throws InterruptedException, IOException {
        //Create and run server
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    Server server = new Server(port);
                    server.serve();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        //Wait one second to (hopefully) give server ample time to be created
        Thread.sleep(1000);
        //Create and run client:
        Socket socket = new Socket(InetAddress.getByName(serverName), port);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        //The new board is sent:
        assertEquals(in.readLine(), "NEWBOARD IHTFP 640 480");
        assertEquals(in.readLine(), "ENDEDITS");
        
        //All boards and all users are sent:
        assertEquals(in.readLine(), "ALLBOARDS IHTFP");
        assertEquals(in.readLine(), "ALLUSERS newuser0");
        
        //pushToClients is called at the end of newUser():
        assertEquals(in.readLine(), "ALLUSERS newuser0");
        assertEquals(in.readLine(), "USERSONBOARD IHTFP newuser0");
        
        //We'll make a client user to check newuser naming conventions
        Socket socketQuick = new Socket(InetAddress.getByName(serverName), port);
        BufferedReader inQuick = new BufferedReader(new InputStreamReader(socketQuick.getInputStream()));
        PrintWriter outQuick = new PrintWriter(socketQuick.getOutputStream(), true);
        
        //Check, primarily, that "newuser1" was assigned to the newest client
        String quickLine = in.readLine();
        assertTrue(quickLine.startsWith("ALLUSERS"));
        assertTrue(quickLine.contains("newuser0"));
        assertTrue(quickLine.contains("newuser1"));
        quickLine = in.readLine();
        assertTrue(quickLine.startsWith("USERSONBOARD IHTFP"));
        assertTrue(quickLine.contains("newuser0"));
        assertTrue(quickLine.contains("newuser1"));
        
        //Quick client (newuser1) disconnects:
        socketQuick.close();
        inQuick.close();
        outQuick.close();
        
        //Check that original client was updated of newuser1's departure:
        assertEquals(in.readLine(), "ALLUSERS newuser0");
        assertEquals(in.readLine(), "USERSONBOARD IHTFP newuser0");
        
        //Draw two things
        String drawCommand1 = "DRAW SEGMENT IHTFP newuser0 100 100 40 40 -65535 20";
        String drawCommand2 = "DRAW SEGMENT IHTFP newuser0 1 1 15 18 -60000 14";
        out.println(drawCommand1);
        assertEquals(in.readLine(), drawCommand1);
        out.println(drawCommand2);
        assertEquals(in.readLine(), drawCommand2);
        
        //Chat!
        String chatCommand = "CHAT IHTFP newuser0 18:10:15 Hey there, how are you?";
        out.println(chatCommand);
        assertEquals(in.readLine(), chatCommand);
        
        //Make new board
        String newBoardCommand = "ADDBOARD IHTFP mynewboard 251 251";
        out.println(newBoardCommand);
        //Next line should be an ALLBOARDS message
        String nextLine = in.readLine();
        assertTrue(nextLine.startsWith("ALLBOARDS"));
        assertTrue(nextLine.contains("mynewboard"));
        assertTrue(nextLine.contains("IHTFP"));
        //Next line should be a USERSONBOARD message
        assertEquals(in.readLine(), "USERSONBOARD mynewboard newuser0");
        //Next should be a NEWBOARD sequence
        assertEquals(in.readLine(), "NEWBOARD mynewboard 251 251");
        assertEquals(in.readLine(), "ENDEDITS");
        
        //Change username
        String newNameCommand = "USERCHANGENAME awesomecow";
        out.println(newNameCommand);
        assertEquals(in.readLine(), "ALLUSERS awesomecow");
        assertEquals(in.readLine(), "USERSONBOARD mynewboard awesomecow");
        
        //Change back to other board
        String changeBoardCommand = "USERCHANGEBOARD mynewboard IHTFP";
        out.println(changeBoardCommand);
        assertEquals(in.readLine(), "NEWBOARD IHTFP 640 480");
        assertEquals(in.readLine(), drawCommand1);
        assertEquals(in.readLine(), drawCommand2);
        assertEquals(in.readLine(), "ENDEDITS");
        assertEquals(in.readLine(), chatCommand);
        assertEquals(in.readLine(), "USERSONBOARD IHTFP awesomecow");
        
        //Create and run another client:
        Socket socket2 = new Socket(InetAddress.getByName(serverName), port);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
        PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
        
        //The new board is sent:
        assertEquals(in2.readLine(), "NEWBOARD IHTFP 640 480");
        assertEquals(in2.readLine(), drawCommand1);
        assertEquals(in2.readLine(), drawCommand2);
        assertEquals(in2.readLine(), "ENDEDITS");
        
        //Chat history is sent:
        assertEquals(in2.readLine(), chatCommand);
        
        //All boards and all users are sent:
        nextLine = in2.readLine();
        assertTrue(nextLine.startsWith("ALLBOARDS"));
        assertTrue(nextLine.contains("IHTFP"));
        assertTrue(nextLine.contains("mynewboard"));
        nextLine = in2.readLine();
        assertTrue(nextLine.startsWith("ALLUSERS"));
        assertTrue(nextLine.contains("newuser0"));
        assertTrue(nextLine.contains("awesomecow"));
        
        //pushToClients is called at the end of newUser():
        nextLine = in2.readLine();
        assertTrue(nextLine.startsWith("ALLUSERS"));
        assertTrue(nextLine.contains("newuser0"));
        assertTrue(nextLine.contains("awesomecow"));
        nextLine = in2.readLine();
        assertTrue(nextLine.startsWith("USERSONBOARD IHTFP"));
        assertTrue(nextLine.contains("newuser0"));
        assertTrue(nextLine.contains("awesomecow"));
        
        //the original client also gets info from the pushToClients:
        nextLine = in.readLine();
        assertTrue(nextLine.startsWith("ALLUSERS"));
        assertTrue(nextLine.contains("newuser0"));
        assertTrue(nextLine.contains("awesomecow"));
        nextLine = in.readLine();
        assertTrue(nextLine.startsWith("USERSONBOARD IHTFP"));
        assertTrue(nextLine.contains("newuser0"));
        assertTrue(nextLine.contains("awesomecow"));
        
        //Second client changes boards
        String clien2ChangeBoardCommand = "USERCHANGEBOARD IHTFP mynewboard";
        out2.println(clien2ChangeBoardCommand);
        assertEquals(in2.readLine(), "NEWBOARD mynewboard 251 251");
        assertEquals(in2.readLine(), "ENDEDITS");
        assertEquals(in2.readLine(), "USERSONBOARD mynewboard newuser0");
        
        //The first client receives notification of client 2 changing boards:
        assertEquals(in.readLine(), "USERSONBOARD IHTFP awesomecow");

        //First client disconnects
        out.close();
        in.close();
        socket.close();
        
        //The second client should receive updates about the first disconnecting:
        assertEquals(in2.readLine(), "ALLUSERS newuser0");
        //(But no message about the board since on a different board.)
        
        //Close connection
        out2.close();
        in2.close();
        socket2.close();
    }

}
