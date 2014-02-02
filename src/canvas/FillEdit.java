package canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The FillEdit is an abstract datatype representing an edit for filling
 * up a bounded region of pixels with a given colour.
 * Rep Invariant:
 * 	None of the parameters can be null
 */
public class FillEdit implements Edit {
	/*
	 * Rep Invariant:
	 * 	None of the parameters can be null
	 * Abstraction Function:
	 * 	The FillEdit represents an flood fill 
	 * 		drawn on a board with name boardID
	 *  	by a user with username userID
	 *  	filling the region containing the point (startX, startY)
	 *  	with the color color.
	 */
	private String boardID;
	private String userID;
	private Integer startX;
	private Integer startY;
	private Color color;
	
	/**
	 * Constructs a FillEdit object with the board name, username, starting x and y
	 * coordinates, and the color to fill the region containing the starting coordinates
	 * Precondition:
	 * 	startX, startY must be greater than or equal to 0
	 * 	boardID cannot be an empty String
	 * 	userID cannot be an emptyString
	 * @param boardID String name of board on which the FillEdit was made
	 * @param userID String username of user responsible for making the FillEdit
	 * @param startX int x-coordinate of seed point for the FillEdit
	 * @param startY int y-coordinate of seed point of SegmentEdit
	 * @param color Color color with which to fill the bounded region when applying the FillEdit
	 */
	public FillEdit(String boardID, String userID, int startX, int startY, Color color){
		this.boardID = boardID;
		this.userID = userID;
		this.startX = startX;
		this.startY = startY;
		this.color = color;
	}
	
	/**
	 * Checks if the x and y coordinates are within the bounds of the Image
	 * @param x int x-coordinate of the point being tested
	 * @param y int y-coordinate of the point being tested
	 * @param im Image the bounds of which x and y must fall within
	 * @return boolean for whether x and y fall within the dimensions of Image im
	 */
	public boolean withinBounds(int x, int y, Image im){
		int x0 = x;
		int y0 = y;
		int maxW = im.getWidth(null)-1;
		int maxH = im.getHeight(null)-1;
		return x0>=0 && x0<=maxW && y0>=0 && y0<=maxH;
	}
	
	/**
	 * Returns a String containing all the information that defines a FillEdit
	 * following the format defined by the line protocol, including the following:
	 * "FILL", boardID, userID, startX, startY, and the int RGB value of color
	 */
	@Override
	public String serialize() {
		return "FILL " + boardID + " " + userID + " " + 
				startX + " "  + startY + " " + color.getRGB();
	}
	
	/**
	 * Fills the bounded region containing startX and startY with the color
	 * specified by the FillEdit's attributes.
	 */
	@Override
	public void apply(Image im){
		ArrayList<Point> pointQ = new ArrayList<Point>();
		pointQ.add(new Point(startX, startY));
		int originColor = ((BufferedImage) im).getRGB(startX, startY);
		while(!pointQ.isEmpty()){
			int xR = (int) pointQ.get(0).getX();
			int xL = xR-1;
			int y = (int) pointQ.get(0).getY();
			pointQ.remove(0);
			while(withinBounds(xR,y,im)&&((BufferedImage)im).getRGB(xR, y) == originColor){
				((BufferedImage) im).setRGB(xR, y, color.getRGB());
				if(withinBounds(xR,y+1,im)&&((BufferedImage)im).getRGB(xR, y+1) == originColor){
					pointQ.add(new Point(xR, y+1));
				}
				if(withinBounds(xR,y-1,im)&&((BufferedImage)im).getRGB(xR, y-1) == originColor){
					pointQ.add(new Point(xR, y-1));
				}
				xR++;
			}
			while(withinBounds(xL,y,im)&&((BufferedImage)im).getRGB(xL, y) == originColor){
				((BufferedImage) im).setRGB(xL, y, color.getRGB());
				if(withinBounds(xL,y+1,im)&&((BufferedImage)im).getRGB(xL, y+1) == originColor){
					pointQ.add(new Point(xL, y+1));
				}
				if(withinBounds(xL,y-1,im)&&((BufferedImage)im).getRGB(xL, y-1) == originColor){
					pointQ.add(new Point(xL, y-1));
				}
				xL--;
			}
			
		}
	}
	
	//Accessor Methods - present for the purpose of maintainability
	/**
	 * Returns an arrayList of Objects including:
	 * 	canvasID, userID, startX, startY, and color
	 */
	@Override
	public Object[] getParams() {
		return new Object[] {boardID, userID, startX, startY, color};
	}

	/**
	 * Returns the String boardID of the FillEdit
	 */
	@Override
	public String getBoardID() {
		return boardID;
	}
	
	/**
	 * Returns the String userID of the FillEdit
	 */
	@Override
	public String getUserID() {
		return userID;
	}
	
	/**
	 * Always returns false, because it is not a SegmentEdit
	 */
	@Override
	public boolean isSegmentEdit() {
		return false;
	}
	
	/**
	 * Always returns true, because it is a FillEdit
	 */
	@Override
	public boolean isFillEdit() {
		return true;
	}

}
