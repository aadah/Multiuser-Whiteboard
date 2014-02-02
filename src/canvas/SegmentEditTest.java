package canvas;

import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import org.junit.Test;

public class SegmentEditTest {
	/*
	 * Testing strategy:
	 * 	Construct a SegmentEdit using both constructors
	 * 	For each SegmentEdit constructed:
	 * 		Check that serialize returns the expected String
	 * 		Check that each accessor method returns the expected Object
	 * 		Check that each is-a method correctly identifies the SegmentEdit
	 *  
	 * 	apply(Image) is tested manually through what is displayed on the
	 *  GUI after the SegmentEdit has been applied
	 */

	//Testing the first constructor and each accessor and type verification method
	@Test
	public void firstConstructorTest(){
		// Constructs a SegmentEdit using the first constructor
		SegmentEdit segEd1 = new SegmentEdit("board1", "user1", 2013, 0, 143, 12, Color.BLUE, 10);
		String expectedSerializeOutput = "SEGMENT board1 user1 2013 0 143 12 "+Color.BLUE.getRGB()+" 10";
		String expectedBoardID = "board1";
		String expectedUserID = "user1";
		int expectedX1 = 2013;
		int expectedY1 = 0;
		int expectedX2 = 143;
		int expectedY2 = 12;
		Color expectedColor = Color.BLUE;
		Stroke expectedStroke = new BasicStroke(10,1,1);
		Object[] expectedParams = 
				new Object[] {"board1","user1",2013,0,143,12,Color.BLUE,((Stroke)new BasicStroke(10,1,1))};
		
		
		assertEquals(segEd1.serialize(),expectedSerializeOutput);
		assertArrayEquals(segEd1.getParams(),expectedParams);
		assertEquals(segEd1.getBoardID(),expectedBoardID);
		assertEquals(segEd1.getUserID(),expectedUserID);
		assertEquals(segEd1.getX1(),expectedX1);
		assertEquals(segEd1.getY1(),expectedY1);
		assertEquals(segEd1.getX2(),expectedX2);
		assertEquals(segEd1.getY2(),expectedY2);
		assertEquals(segEd1.getColor(),expectedColor);
		assertEquals(segEd1.getStroke(),expectedStroke);
		assertEquals(segEd1.isSegmentEdit(),true);
		assertEquals(segEd1.isFillEdit(),false);
	}
	
	//Testing the second constructor and each accessor and type verification method
		@Test
		public void secondConstructorTest(){
			// Constructs a SegmentEdit using the first constructor
			SegmentEdit segEd1 = new SegmentEdit("board2", "user2", 1, 2, 4, 35, Color.GREEN, 6);
			String expectedSerializeOutput = "SEGMENT board2 user2 1 2 4 35 "+Color.GREEN.getRGB()+" 6";
			String expectedBoardID = "board2";
			String expectedUserID = "user2";
			int expectedX1 = 1;
			int expectedY1 = 2;
			int expectedX2 = 4;
			int expectedY2 = 35;
			Color expectedColor = Color.GREEN;
			Stroke expectedStroke = new BasicStroke(6,1,1);
			Object[] expectedParams = 
					new Object[] {"board2","user2",1,2,4,35,Color.GREEN,((Stroke) new BasicStroke(6,1,1))};
			
			
			assertEquals(segEd1.serialize(),expectedSerializeOutput);
			assertArrayEquals(segEd1.getParams(),expectedParams);
			assertEquals(segEd1.getBoardID(),expectedBoardID);
			assertEquals(segEd1.getUserID(),expectedUserID);
			assertEquals(segEd1.getX1(),expectedX1);
			assertEquals(segEd1.getY1(),expectedY1);
			assertEquals(segEd1.getX2(),expectedX2);
			assertEquals(segEd1.getY2(),expectedY2);
			assertEquals(segEd1.getColor(),expectedColor);
			assertEquals(segEd1.getStroke(),expectedStroke);
			assertEquals(segEd1.isSegmentEdit(),true);
			assertEquals(segEd1.isFillEdit(),false);
		}
}
