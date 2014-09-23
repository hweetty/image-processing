
/** Stores a grid of Pixel objects that form an image. It can also perform effects
 * such as Flip, Blur, Sharpen, Resize, and Shape Detection.
 * @author  Jerry Yu 
 * @version	Oct. 23, 2012
 */
 
import java.awt.*;
import java.awt.image.*;
import javax.swing.ImageIcon;
import java.util.*;

public class Picture
{
	// Constants representing the colours for flood fill
	final public int DEFAULT_FILL_COLOUR  	= 0xFF9B18E0;		// Purple
	final public int RECT_COLOUR 			= 0xFF00E8FF;		// Cyan
	final public int TRI_COLOUR  			= 0xFF72FF00;		// Green-yellow
	final public int CIRCLE_COLOUR    		= 0xFFFF00DC;		// Pink
	
	private Image image;
	private Pixel[][] picture;
	private Container container;

	/** Creates a new Picture object from a file image
	 * @param fileName The name of the image file
	 * @param container The container (e.g. Frame) you will be displaying the image in
	 */
	public Picture(String fileName, Container container)
	{
		// Load up the image and get its width and height
		image = new ImageIcon(fileName).getImage();
		int width = image.getWidth(container);
		int height = image.getHeight(container);
		this.container = container;

		// Grab the pixel values (32 bit integers) from the Image
		int[] pixels = new int[width * height];
		picture = new Pixel[height][width];
		PixelGrabber pixelGrab = new PixelGrabber(image, 0, 0, width, height,
				pixels, 0, width);
		try
		{
			pixelGrab.grabPixels();
		}
		catch (InterruptedException exp)
		{
			System.err.println("Error grabbing pixels");
		}

		// Translate the 1D array of image data into a 2D array of Pixels
		for (int i = 0; i < pixels.length; i++)
			picture[i / width][i % width] = new Pixel(pixels[i]);
	}

	/** Copy constructor. Constructs a new Picture object that is a copy of
	 *    	another Picture object
	 * @param other the Picture object to make a copy of
	 */
	public Picture(Picture other)
	{
		// Create a new 2D array that is a copy of the other Picture's array
		picture = new Pixel[other.picture.length][other.picture[0].length];
		for (int row = 0; row < other.picture.length; row++)
			for (int col = 0; col < other.picture[0].length; col++)
			{
				picture[row][col] = new Pixel(other.picture[row][col]);
			}
		// Assume new Picture has the same container
		this.container = other.container;

		// Update the image to match the 2D array of Pixels
		updateImage();
	}

	/** Get the Pixel object at a coordinate
	 * @param x The x coordinate of the Pixel
	 * @param y The y coordinate of the Pixel
	 * @return The Pixel object at the given coordinate if it is within bounds,
			null otherwise
	 */
	public Pixel getPixel(int x, int y)
	{
		if (x < 0 || x >= picture[0].length ||
			y < 0 || y >= picture.length)
			return null;
		else
			return picture[y][x];
	}

	/** Get the number of pixels wide of this image
	 * @return The number of the number of pixels wide of this image
	 */
	public int getWidth()
	{
		return picture[0].length;
	}

	/** Get the number of pixels high of this image
	 * @return The number of the number of pixels high of this image
	 */
	public int getHeight()
	{
		return picture.length;
	}

	
	/*   	Begin Effects 		*/
	
    /** Checks if two colours are similar. Does this by comparing the RBG and
	 * 		alpha values.
     * @param c1 An array of 4 ints describing the Alpha, Red, Green, Blue values
     * @param c2 An array of 4 ints describing the Alpha, Red, Green, Blue values
     * @return true if the maximum difference in all values of both colours is less
			than a certain value (10), false otherwise.
     */
	public static boolean isSimilarcolour (int [] c1, int [] c2)
	{
		for (int i = 0; i < c1.length; i ++)
			if (Math.abs(c1[i]-c2[i]) >= 10)
				return false;						// Return as soon as found different colour
	
		return true;
	}
	
	/** Fills all surrounding pixels of similar colours as the Pixel at
			the given coordinate
	 * @param x The x coordinate of the pixel to start
	 * @param y The y coordinate of the pixel to start
	 * @param colour The colour to fill in, represented by a 32 bit int.
	 */
	public void floodFill(int x, int y, int fillColour)
	{
		// Remember this colour
		int [] colour = picture[y][x].getArrayFromPixels();
		// Store points previously visited
		HashSet <Point> visited = new HashSet <Point> ();
		// Store points to visit next
		Queue <Point> q = new LinkedList <Point> ();
		// Begin with current point
		q.add(new Point(x, y));
		
		while (!q.isEmpty())
		{
			Point coord = q.remove();				// Get top element and remove it too
			if (visited.add(coord))					// Add and make sure it wasnt visited
			{
				// Similar colours?
				if (isSimilarcolour(colour, picture[coord.y][coord.x].getArrayFromPixels()))	
				{
					// Fill
					picture[coord.y][coord.x].setColour(fillColour);
					
					// Add surrounding pixels (if possible)
					if (coord.x > 0)
						q.add(new Point (coord.x-1, coord.y));
					if (coord.y > 0)
						q.add(new Point (coord.x, coord.y-1));
					if (coord.x < getWidth()-1)
						q.add(new Point (coord.x+1, coord.y));
					if (coord.y < getHeight()-1)
						q.add(new Point (coord.x, coord.y+1));
				}
			}	// Check not visited
		}	// While loop
	}

	/** The default flood fill (called from GUI). Uses the helper method above.
	 * @param x The x coordinate of the pixel to start
	 * @param y The y coordinate of the pixel to start
	 */
	public void floodFill(int x, int y)
	{
		// Use helper method with default colour
		floodFill(x, y, DEFAULT_FILL_COLOUR);
	}
	
	/** Swaps all the pixels horizontally such that the resulting image formed
	 *		is reflected along x = m, where m is the x coordinate of the center pixel
	 */
	public void flip()
	{
		for (int row = 0 ; row < picture.length; row ++)
		{
			for (int col = 0 ; col < picture[0].length / 2; col ++)	// Only go halfway
			{
				Pixel p = picture[row][col];	// Store temporarily
				picture[row][col] = picture[row][picture[row].length -1 - col];
				picture[row][picture[row].length -1 - col] = p;
			}
		}
		
		updateImage();
	}

	/** Change all pixels such that the new value of each RGB value is
	 *		255 minus the old value
	 */
	public void negative()
	{
		for (int row = 0 ; row < picture.length; row ++)
			for (int col = 0 ; col < picture[0].length; col ++)
				picture[row][col].negative();		// Do calculations in Pixel class
		
		updateImage();
	}
	
	/** Given the top left position in a shape, this method determines the type
	 *		of shape and fills it in with the appropriate colour.
	 * 	   (The steps required are difficult to explain without diagrams. I can
	 * 		explain all steps in class if needed.)
	 * @param x The x coordinate of the top left position in a shape
	 * @param y The y coordinate of the top left position in a shape
	 */
	public void fillShape (int x, int y)
	{
		// Remember this colour
		int colour = picture[y][x].getColour();
		// Store points with similar colour
		HashSet <Point> visited = new HashSet <Point> ();
		// Store points to visit next
		Queue <Point> q = new LinkedList <Point> ();
		// Begin with current point
		q.add(new Point(x, y));
		
		// Remember the left/right/top/bottom most position (init with default values)
		int startX = 1000000;
		int endX = -1;
		int startY = 1000000;
		int endY = -1;
		
		// Maintain an array that holds the top and bottom most y coord at given x
		// (if exists). Similar, it keeps track of the left/right most x coord
		// at given y coord
		Point [][]hStats = new  Point [getWidth()][2];
		Point [][]vStats = new  Point [getHeight()][2];
		
		// Fill with default values
		for (int i = 0; i < hStats.length; i ++)
		{
			hStats[i][0] = new Point(i, getHeight()-1);
			hStats[i][1] = new Point(i, 0);
		}
		for (int i = 0; i < vStats.length; i ++)
		{
			vStats[i][0] = new Point(getWidth()-1, i);
			vStats[i][1] = new Point(0, i);
		}
		
		// Begin searching
		while (!q.isEmpty())
		{
			Point coord = q.remove();				// Get top element and remove it too
			// Add and make sure it wasnt visited
			if (!visited.contains(coord) && picture[coord.y][coord.x].getColour() == colour)	
			{
				visited.add(coord);
				
				// Check if this coordinate is higher/lower/left/right of stored value
				if (coord.y < hStats[coord.x][0].y)
					hStats[coord.x][0] = coord;
				if (coord.y > hStats[coord.x][1].y)
					hStats[coord.x][1] = coord;
				if (coord.x < vStats[coord.y][0].x)
					vStats[coord.y][0] = coord;
				if (coord.x > vStats[coord.y][1].x)
					vStats[coord.y][1] = coord;
				
				// Check if this coordinate is higher/lower/left/right of "extremes"
				if (coord.x < startX)
					startX = coord.x;
				if (coord.x > endX)
					endX = coord.x;
				if (coord.y < startY)
					startY = coord.y;
				if (coord.y > endY)
					endY = coord.y;
					
				
				// Add surrounding pixels to visit
				if (coord.x > 0)
					q.add(new Point (coord.x-1, coord.y));
				if (coord.y > 0)
					q.add(new Point (coord.x, coord.y-1));
				if (coord.x < getWidth()-1)
					q.add(new Point (coord.x+1, coord.y));
				if (coord.y < getHeight()-1)
					q.add(new Point (coord.x, coord.y+1));
			}	// Check not visited
		}	// While loop
		
		int maxYDiff = 0;		// Max y diffs
		int yOcc = 0;			// Max occurences of this y diff
		int yPars = 0;			// Number of vertical parellel lines
		for (int i = startX; i < endX; i ++)
		{
			// Check difference in top/bottom y values at given x
			int diff = (hStats[i][1].y - hStats[i][0].y) / 2;
			if (diff > maxYDiff)
			{
				maxYDiff = diff;	// Found a longer difference
				yOcc = 1;
			}
			else if (diff == maxYDiff)
				yOcc ++;			// Same difference, increment
			
			// Check for parellel lines
			Point A1 = hStats[i][0];
			Point B1 = hStats[i][1];
			for (int j = i + 3; j < endX; j += 3)
			{
				Point A2 = hStats[j][0];
				Point B2 = hStats[j][1];
				
				if (isParellel (A1, A2, B1, B2))
					yPars++;		// Found another parellel line
			}
		}
		
		int maxXDiff = 0;		// Max y diffs
		int xOcc = 0;			// Max occurences of this y diff
		int xPars = 0;			// Number of horizontal parellel lines
		for (int i = startY; i < endY; i ++)	// Same loop as above except looping down
		{
			int diff = (vStats[i][1].x - vStats[i][0].x) / 2;
			if (diff > maxXDiff)
			{
				maxXDiff = diff;
				xOcc = 1;
			}
			else if (diff == maxXDiff)
				xOcc ++;
				
			Point A1 = vStats[i][0];
			Point B1 = vStats[i][1];
			for (int j = i + 3; j < endY; j += 3)
			{
				Point A2 = vStats[j][0];
				Point B2 = vStats[j][1];
				
				if (isParellel (A1, A2, B1, B2))
					xPars++;
			}
		}
		
		// Number of parellel lines compared to the maximum combined "atlitude"
		double percentage = 50.0 * (yPars + xPars) / (maxYDiff * maxXDiff);
		
		int fillColour = CIRCLE_COLOUR; // Default circle
		if (percentage < 10 && xOcc < 10 && yOcc < 10)
			fillColour = TRI_COLOUR;	// Few occurences of parellel lines
		else if (percentage < 30 || xOcc >= 50 || yOcc >= 50)
			fillColour = RECT_COLOUR;	// Rectangles have more occurences of max diff
		
		// Fill all the pixels that were visited previously with the shape colour
		for (Point p : visited)
			picture[p.y][p.x].setColour(fillColour);
	}
	
	/** Determines if two lines, each connected by 2 points, are parellel
	 * @param A1 The first Point on the first line
	 * @param A2 The second Point on the first line
	 * @param B1 The first Point on the second line
	 * @param B2 The second Point on the second line
	 * @return true if the slope of A1 to A2 is equal to the slope of
			B1 to B2, false otherwise
     */
	public boolean isParellel (Point A1, Point A2, Point B1, Point B2)
	{
		int A = Math.abs((A2.y-A1.y) * (B2.x-B1.x));	// Do not use division
		int B = Math.abs((A2.x-A1.x) * (B2.y-B1.y));	// Instead, cross-multiply
		return Math.abs(A - B) == 0;					// Parellel if diff is zero
	}
	
    /** Called by the GUI. This method scanns the image and fills in all
	 * 		rectangles, triangles, and circles.
     * Preconditions:
	 *		1) The image only contains rectangles (including trapezoids),
	 *		triangles, and circles.
	 *		2) The top left pixel (0,0) is assumed to be the background colour.
	 *		3) The minimum size a shape should be is 55x55 pixels.
     */
	public void shapes ()
	{
		int backColour = picture[0][0].getColour();
		
		for (int y = 0; y < getHeight()-1; y ++)
		{
			for (int x = 0; x < getWidth()-1; x ++)
			{
				// Continue if found a background coloured pixel or already filled
				if (picture[y][x].getColour() == backColour || 
					picture[y][x].getColour() == RECT_COLOUR ||
					picture[y][x].getColour() == TRI_COLOUR ||
					picture[y][x].getColour() == CIRCLE_COLOUR)
					continue;
				
				// Use helper method to determine and fill shape
				fillShape(x, y);
			}
		}
		
		updateImage();
	}
	
	/** Get a new 2d array of Pixels that is transformed
	 * @return A new 2d array of Pixels that is transformed that is similar to
	 *		flipping horizontally, then rotating counter-clockwise by 90 degrees
	 *		top left & bottom right remains
	 *		top right swaps with bottom left
	 */
	public Pixel [][] getTransformed(Pixel [][] old)
	{
		Pixel [][] flipped = new Pixel[old[0].length][old.length];
		for (int y = 0; y < flipped.length; y ++)
			for (int x = 0; x < flipped[y].length; x ++)
				flipped[y][x] = old[x][y];
		
		return flipped;
	}
	
	/** Modifies all the pixel values so that each pixel is the average of all
	 *		surrounding pixels
	 * @param r The rumber of pixels in each direction to include in the average
	 */
	public void blurHorizontal (int r) 
	{
		if (r % 2 == 0)
			r++;		// Even radius shifts the image, change to an odd number.
			
		// Horizontal pass
		for (int y = 0; y < picture.length; y ++)
		{
			// Use a LinkedList to easily get the trailing leftmost array of colours
			LinkedList <Object> q = new LinkedList <Object> ();
			int []total = new int [4];
			
			// Add the leftmost array of colours
			int [] arr = picture[y][0].getArrayFromPixels();
			Pixel.addPixelArrays(total, arr, 1);
			q.add(arr);
			
			for (int x = r/2; x < picture[y].length-1; x ++)
			{
				// Add the colour array at this x index
				arr = picture[y][x].getArrayFromPixels();
				Pixel.addPixelArrays(total, arr, 1);
				q.add(arr);
				
				// Set the average colour of the middle pixel (depending on radius)
				picture[y][x-r/2].setColour(Pixel.getAverageValue(total, q.size()));
				
				if (q.size() >= r)
					// Remove the trailing leftmost colour if needed
					Pixel.addPixelArrays(total, (int[])q.remove(), -1);
			}
			
			// Blur the rightmost pixel
			// (do not need to add any pixels, so just take average)
			picture[y][getWidth()-1].setColour(Pixel.getAverageValue(total, q.size()));
		}
	}
	
	/** This method is called by the GUI. It then calls the above helper method
	 *		to blur horizontally, transform the image, blur and finally transform again.
	 * 		The extra transformations do not make a huge difference in time required,
	 *		and reduces duplicate code.
	 */
	public void blur ()
	{
		int radius = 3;
		
		// Blur (the horizontal) and transform
		blurHorizontal (radius);
		picture = getTransformed(picture);
		
		// Blur (the vertical) and transform back
		blurHorizontal (radius);
		picture = getTransformed(picture);
		
		updateImage();
	}
	
	/** Sharpens the image by going through all Pixels and calculating a new
	 *		colour by weighting it and nearby Pixels more than farther Pixels
	 */
	public void sharpen()
	{
		Pixel [][] newPic = new Pixel[getHeight()][getWidth()];
		int [][] matrix = { {-1, -1, -1, -1, -1},		// Weightings for
							{-1,  2,  2,  2, -1},		// neighbouring Pixels
							{-1,  2, 16,  2, -1},		// (5x5 grid)
							{-1,  2,  2,  2, -1},
							{-1, -1, -1, -1, -1} };
							
		for (int y = 0; y < picture.length; y ++)
		{
			for (int x = 0; x < picture[y].length; x ++)
			{
				// Get weighted average of neighbouring Pixels
				int []total = new int[4];
				int totalWeight = 0;
				
				for (int dy = -2; dy <= 2; dy ++)
				{
					if (y + dy < 0 || y + dy >= getHeight())
						continue;		// Continue if out of bounds
						
					for (int dx = -2; dx <= 2; dx ++)
					{
						if (x + dx < 0 || x + dx >= getWidth())
							continue;	// Continue if out of bounds
						
						// Get the weighting for this Pixel
						int weight = matrix[dy+2][dx+2];
						// Add the weighted value to the total
						Pixel.addPixelArrays(total,
							picture[y+dy][x+dx].getArrayFromPixels(),
							weight);
						// Also add the total weightings to the total
						totalWeight += weight;
					}
				}
				
				// Create a new Pixel with the calculated weighted average
				newPic[y][x] = new Pixel(Pixel.getAverageValue(total, totalWeight));
			}
		}
		
		// Assign the picture reference to the new array and update image
		picture = newPic;
		updateImage();
	}

	/** Changes each pixel so that the new RGB values is the weighted average
	 *		of the old values
	 */
	public void blackAndWhite()
	{
		for (int y = 0; y < picture.length; y ++)
			for (int x = 0; x < picture[0].length; x ++)
				picture[y][x].blackWhite();		// Do calculations in Pixel class
		
		updateImage();
	}

	/** Creates a new picture with a size that is half as wide and tall as
	 *		the old picture and display it
	 */
	public void shrink()
	{
		if (getHeight() <= 1 || getWidth() <= 1)
			return;			// Prevents exceptions if trying to shrink small images
		
		// Assume new Image size is always half
		Pixel [][] newPic = new Pixel [getHeight()/2][getWidth()/2];
		for (int y = 0; y < newPic.length; y ++)
		{
			for (int x = 0; x < newPic[y].length; x ++)
			{
				// "Divide" the old image into blocks of 2x2 Pixels
				// and get the average colour
				int [] total = picture[2*y][2*x].getArrayFromPixels();
				Pixel.addPixelArrays(total, picture[2*y][2*x+1].getArrayFromPixels(), 1);
				Pixel.addPixelArrays(total, picture[2*y+1][2*x].getArrayFromPixels(), 1);
				Pixel.addPixelArrays(total, picture[2*y+1][2*x+1].getArrayFromPixels(), 1);
				
				// Create a new Pixel with the calculated weighted average
				newPic[y][x] = new Pixel (Pixel.getAverageValue(total, 4));
			}
		}
		
		// Assign the picture reference to the new array and update image
		picture = newPic;
		updateImage();
	}

	/**
	 * Updates the Image for this Picture using the data in the 2D array of
	 * Pixels. This method needs to be called every time the picture data
	 * changes so that all changes will be displayed in the main program
	 */
	public void updateImage()
	{
		// Create a 1D array of picture data
		int width = picture[0].length;
		int height = picture.length;
		int[] pixels = new int[width * height];

		// Fill in the 1D array of integers with the data from
		// the 2D array of Pixels
		int pixel = 0;
		for (int row = 0; row < height; row++)
			for (int col = 0; col < width; col++)
			{
				pixels[pixel++] = picture[row][col].getColour();
			}

		// Create the image based on the data in the 1D array
		image = container.createImage(new MemoryImageSource(width, height,
				pixels, 0, width));
	}

	/**
	 * Draws this Picture's image in the given Graphics context with the upper
	 * left corner of the image in the given position
	 * 
	 * @param g
	 *            the Graphics context to draw this Picture
	 * @param x
	 *            the x-coordinate of the Picture's upper left corner
	 * @param y
	 *            the y-coordinate of the Picture's upper left corner
	 */
	public void draw(Graphics g, int x, int y)
	{
		g.drawImage(image, x, y, container);
	}

}



