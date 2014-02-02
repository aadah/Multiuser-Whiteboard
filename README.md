# Multiuser Whiteboard

Second of two team projects during 6.005, Fall 2013.

To start up, open the project in [Eclipse](http://www.eclipse.org/) or other Java
IDE capable of compiling from source. Run the server first.
To do that, compile and run `src/server/Server.java` on the
machine that you wish to designate as the server.

To start up a client session, run `src/client/MasterGUI.java`.
The first window will ask for the machine to connect to. Simply
enter the server's IPv4 address. Next, enter a session username
(not permanent). You'll then be brought to a default whiteboard
shared by all connected users. From here on out, you can do things
like create new whiteboards, chat with users on your current board,
and save whatever you've drawn to disk as a PNG file.

The application does not persist, so any server error that causes a
crash will also result in loss of all whiteboards, chat logs, etc.
