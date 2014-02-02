package canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

/**
 * The SegmentEdit is an abstract datatype representing a line edit and
 * can be used for drawing line "differentials" for free-hand drawing as
 * well as for drawing line segments.
 */
public class SegmentEdit implements Edit {
	/*
	 * Rep Invariant:
	 * 	None of the parameters can be null
	 * 	size should be equal to the width of Stroke
	 * Abstraction Function:
	 * 	The SegmentEdit represents a line 
	 * 		drawn on a board with name boardID
	 *  	by a user with username userID
	 *  	starting at the point (startX, startY)
	 *  	and ending at the point (endX, endY)
	 *  	that has the color color
	 *  	and was drawn with a stroke stroke
	 *  	or a stroke that has thickness size
	 */
	private String boardID;
	private String userID;
	private Integer startX;
	private Integer startY;
	private Integer endX;
	private Integer endY;
	private Color color;
	private Integer size;
	private Stroke stroke;
	
	/**
	 * Constructs a SegmentEdit object with the board name, username, starting x and y
	 * coordinates, ending x and y coordinate, color of the line to be drawn when 
	 * the SegmentEdit is applied, and the size of the Stroke to be used for drawing
	 * the line.
	 * Precondition:
	 * 	startX, startY, endX, endY must be greater than or equal to 0
	 * 	size must be greater than 0
	 * 	boardID cannot be an empty String
	 * 	userID cannot be an emptyString
	 * @param boardID String name of board on which the SegmentEdit was made
	 * @param userID String username of user responsible for making the SegmentEdit
	 * @param startX int x-coordinate of starting point of SegmentEdit
	 * @param startY int y-coordinate of starting point of SegmentEdit
	 * @param endX int x-coordinate of ending piont of SegmentEdit
	 * @param endY int y-coordinate of ending point of SegmentEdit
	 * @param color Color color to be used when applying the SegmentEdit
	 * @param size int width of the stroke to be used when applying the SegmentEdit
	 */
	public SegmentEdit(String boardID, String userID, int startX, int startY, 
						int endX, int endY, Color color, int size){
		this.boardID = boardID;
		this.userID = userID;
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.color = color;
		this.size = size;
		this.stroke = new BasicStroke(size,1,1);
	}
	
	/**
	 * This is an overloaded constructor that constructs a SegmentEdit with all the 
	 * same parameters but the last --- instead of taking an int size to define the
	 * Stroke to be used when applying the SegmentEdit, this constructor directly takes
	 * in the Stroke object and extracts int size from it.
	 * @param boardID String name of board on which the SegmentEdit was made
	 * @param userID String username of user responsible for making the SegmentEdit
	 * @param startX int x-coordinate of starting point of SegmentEdit
	 * @param startY int y-coordinate of starting point of SegmentEdit
	 * @param endX int x-coordinate of ending piont of SegmentEdit
	 * @param endY int y-coordinate of ending point of SegmentEdit
	 * @param color Color color to be used when applying the SegmentEdit
	 * @param stroke Stroke to be used when applying the SegmentEdit
	 */
	public SegmentEdit(String boardID, String userID, int startX, int startY, 
			int endX, int endY, Color color, Stroke stroke){
		this.boardID = boardID;
		this.userID = userID;
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.color = color;
		this.size = (int) ((BasicStroke) stroke).getLineWidth();
		this.stroke = stroke;
	}
	
	/**
	 * Returns a String containing all the information that defines a SegmentEdit
	 * following the format defined by the line protocol, including the following:
	 * "SEGMENT", boardID, userID, startX, startY, endX, endY, the int RGB value of color,
	 * and size
	 */
	@Override
	public String serialize() {
		String s = "SEGMENT " + boardID + " " + userID + " " + startX
		        + " " + startY + " " + endX + " " + endY + " " + color.getRGB() + " " + size;
		return s;
	}
	
	/**
	 * Draws a segment from the starting coordinates of the SegmentEdit
	 * to the ending coordinates of the SegmentEdit on the image passed
	 * in as a parameter using the color and stroke specified by the 
	 * SegmentEdit's attributes.
	 */
	@Override
	public void apply(Image im){
		Graphics2D g = (Graphics2D) im.getGraphics();
	    g.setColor(color);
	    g.setStroke(stroke);
	    g.drawLine(startX, startY, endX, endY);
	}

	//Accessor Methods - present for the purpose of maintainability
	/**
	 * Returns an arrayList of Objects including:
	 * 	canvasID, userID, startX, startY, endX, endY, color, size, and stroke
	 */
	@Override
	public Object[] getParams() {
		return new Object[] {boardID, userID, startX, startY, endX, endY, color, stroke};
	}
	
	/**
	 * Returns the String boardID of the SegmentEdit
	 */
	@Override
	public String getBoardID(){
		return boardID;
	}
	
	/**
	 * Returns the String userID of the SegmentEdit
	 */
	@Override
	public String getUserID() {
		return userID;
	}
	
	/**
	 * Gets the startX of the SegmentEdit
	 * @return int startX, which is the x-coordinate of the starting point
	 * of the segment represented by this SegmentEdit
	 */
	public int getX1(){
		return startX;
	}
	
	/**
	 * Gets the startY of the SegmentEdit
	 * @return int startY, which is the y-coordinate of the starting point
	 * of the segment represented by this SegmentEdit
	 */
	public int getY1(){
		return startY;
	}
	
	/**
	 * Gets the endX of the SegmentEdit
	 * @return int endX, which is the x-coordinate of the ending point
	 * of the segment represented by this SegmentEdit
	 */
	public int getX2(){
		return endX;
	}
	
	/**
	 * Gets the endY of the SegmentEdit
	 * @return int endY, which is the y-coordinate of the ending point
	 * of the segment represented by this SegmentEdit
	 */
	public int getY2(){
		return endY;
	}
	
	/**
	 * Gets the SegmentEdit's color
	 * @return Color color specified for the SegmentEdit when calling
	 * the apply(Image) method
	 */
	public Color getColor(){
		return color;
	}
	
	/**
	 * Gets the size of SegmentEdit, which is the same as the width
	 * of the SegmentEdit's stroke
	 * @return int size that defines the width of the SegmentEdit's
	 * stroke
	 */
	public int getSize(){
		return size;
	}

	/**
	 * Gets the SegmentEdit's stroke
	 * @return Stroke stroke specified for the SegmentEdit when calling
	 * the apply(Image) method
	 */
	public Stroke getStroke(){
		return stroke;
	}
	
	/**
	 * Always returns true, because it is a SegmentEdit
	 */
	@Override
	public boolean isSegmentEdit(){
		return true;
	}
	
	/**
	 * Always returns false, because it is not a FillEdit
	 */
	@Override
	public boolean isFillEdit(){
		return false;
	}
}
