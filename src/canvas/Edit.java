package canvas;

import java.awt.Image;

/**
 * An interface requiring serialize(), getParams(), getBoardID(), getUserID(),
 * apply(Image), isSegmentEdit(), isFillEdit(), and isOvalEdit() methods
 * 
 * SegmentEdit, FillEdit, and OvalEdit are classes that implement this interface
 *
 */
public interface Edit {
	
	/**
	 * Returns a string representation of the Edit depending on its
	 * type in such a way that it follows the line protocol for passage
	 * between the Canvas and the server
	 * @return String containing information about the type of the
	 * Edit and the values of each of the parameters that define the Edit
	 */
	public String serialize();
	
	/**
	 * For each type of Edit, a different change should be applied
	 * to the Image that has been passed in.
	 * @param im the Image on which the Edit should be applied
	 */
	public void apply(Image im);

	/**
	 * Gets the parameters that define the Edit
	 * @return an Object array containing the parameters defining
	 * the Edit
	 */
	public Object[] getParams();
	
	/**
	 * Gets the name of the board where the Edit was created
	 * @return String boardName of the board where the Edit
	 * was created
	 */
	public String getBoardID();
	
	/**
	 * Gets the username of the user responsible for creating
	 * the Edit
	 * @return String username of the user that created the Edit
	 */
	public String getUserID();
	
	/**
	 * Checks if an Edit is an instance of SegmentEdit
	 * @return boolean for whether the Edit calling the method
	 * is an instance of SegmentEdit
	 */
	public boolean isSegmentEdit();
	
	/**
	 * Checks if an Edit is an instance of FillEdit
	 * @return boolean for whether the Edit calling the method
	 * is an instance of FillEdit
	 */
	public boolean isFillEdit();
	
}
