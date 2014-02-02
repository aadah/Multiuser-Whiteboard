package server;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;

/**
 * Testing suite for a couple of Server's methods.
 * This testing suite only tests a few of the basic
 * methods defined in Server, such as the server's
 * getServerIP() method. This class does not test
 * client-server interaction. See the other test
 * files (e.g. ServerTests.txt, IntegratedTests.java)
 * for that.
 * 
 * IMPORTANT:
 * Pre-condition for running these tests: that no server
 * is already running on this computer.
 * 
 * @category no_didit
 */
public class ServerMethodTests {
    Server server;
    int port = 4444;
    
    /**
     * Sets up the server
     * 
     * IMPORTANT:
     * Pre-condition: that no server is currently already
     * running on this computer.
     */
    @Before
    public void setup() {
        try {
            server = new Server(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    /**
     * Tests the basic methods of the Server. Note that
     * all tests are in this class, because if they aren't,
     * the JVM throws BindExceptions because it thinks we're
     * trying to connect to the Server's ServerSocket
     * multiple times.
     * 
     * IMPORTANT:
     * Pre-condition: that the only server running on this
     * computer is this class's class variable's Server server.
     */
    @Test
    public void test_basicServerMethods() {
        //Test that the getServerIP method functions correctly
        String IP = server.getServerIP();
        String regexIPExpect = "([0-9]{1,3}.){3}([0-9]{1,3})";
        assertTrue(IP.equals("N/A") || IP.matches(regexIPExpect));
        
        //Test that the validNewUserName method works
        String newUserName = server.validNewUserName();
        assertEquals("newuser0", newUserName);
        
        //Tests that nextUserID works
        assertTrue(server.nextUserID() == 1);
        assertTrue(server.nextUserID() == 2);
        assertTrue(server.nextUserID() == 3);
        
        //Tests that isMessage works appropriately
        String chatMessage = "CHAT myboard myuser 10:11:12 This-is~my !!? message\r\n";
        String drawMessage = "DRAW SEGMENT myboard myuser 1 1 15 18 -60000 14";
        String addboardMessage = "ADDBOARD newboard oldboard 800 1200";
        String changeboardMessage = "USERCHANGEBOARD oldboard newboard";
        String changenameMessage = "USERCHANGENAME mynewname";
        
        assertTrue(server.isMessage(chatMessage, "CHAT"));
        assertTrue(server.isMessage(drawMessage, "DRAW"));
        assertTrue(server.isMessage(addboardMessage, "ADDBOARD"));
        assertTrue(server.isMessage(changeboardMessage, "USERCHANGEBOARD"));
        assertTrue(server.isMessage(changenameMessage, "USERCHANGENAME"));
        
        assertFalse(server.isMessage(chatMessage,  "USERCHANGEBOARD"));
        assertFalse(server.isMessage(drawMessage,  "USERCHANGEBOARD"));
        assertFalse(server.isMessage(changenameMessage,  "DRAW"));
        assertFalse(server.isMessage(addboardMessage,  "USERCHANGENAME"));
        assertFalse(server.isMessage(changeboardMessage,  "CHAT"));
    }

}
